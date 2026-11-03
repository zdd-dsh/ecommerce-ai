package com.example.ecommerce.messaging;

import com.example.ecommerce.utils.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 订单/支付事件消费者。
 *
 * 【激活条件】ecommerce.rabbit.listener-enabled=true 时才生效
 *   （开发环境可不启动消费者，避免无 RabbitMQ 时启动失败）
 *
 * 【重试机制】
 *   - 消费失败时抛 AmqpRejectAndDontRequeueException，消息进入重试队列
 *   - 重试次数由 x-death header 中的 count 字段获取
 *   - 超过 maxRetries（默认 3）次后转入死信队列
 *
 * 【业务示例】这里仅把消息快照存入 Redis（key=mq:last:order:{orderNo}），
 *   实际项目可在此调用通知服务、统计服务、积分服务等。
 */
@Component
@ConditionalOnProperty(value = "ecommerce.rabbit.listener-enabled", havingValue = "true")
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final RedisUtil redisUtil;
    private final OrderMessagePublisher orderMessagePublisher;
    private final String orderCreatedQueue;
    private final String paymentStatusQueue;
    private final int maxRetries;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderEventListener(RedisUtil redisUtil,
                              OrderMessagePublisher orderMessagePublisher,
                              @Value("${ecommerce.rabbit.order-created-queue}") String orderCreatedQueue,
                              @Value("${ecommerce.rabbit.payment-status-queue}") String paymentStatusQueue,
                              @Value("${ecommerce.rabbit.max-retries:3}") int maxRetries) {
        this.redisUtil = redisUtil;
        this.orderMessagePublisher = orderMessagePublisher;
        this.orderCreatedQueue = orderCreatedQueue;
        this.paymentStatusQueue = paymentStatusQueue;
        this.maxRetries = maxRetries;
    }

    /**
     * 监听订单创建队列。
     *
     * @param payload    反序列化后的订单创建消息体（orderNo/userId/totalAmount/productIds）。
     *                   为什么传：业务处理（此处存快照）的数据来源。
     * @param rawMessage RabbitMQ 原始消息（含头信息，如 x-death）。为什么传：process 需要它来读取重试次数。
     */
    @RabbitListener(queues = "${ecommerce.rabbit.order-created-queue}")
    public void onOrderCreated(OrderCreatedMessage payload, Message rawMessage) {
        // 把处理细节（存 Redis 快照 + 记日志）封装成 lambda 传给统一的 process 模板，
        // 复用同一套"重试/死信"逻辑，避免两个监听器各写一遍。
        process(payload.getOrderNo(), orderCreatedQueue, rawMessage, payload, () -> {
            store("mq:last:order:" + payload.getOrderNo(), payload);  // 缓存最新一条消息快照，便于排查
            log.info("Consumed order created message for {}", payload.getOrderNo());
        });
    }

    /**
     * 把消息快照存入 Redis（24h 过期，用于排查问题）。
     * @param key   Redis 键（如 "mq:last:order:{orderNo}"）。为什么传：定位这条消息快照，便于按订单号排查。
     * @param value 要存储的消息体对象。为什么传：序列化为 JSON 后缓存，记录最近一次消息内容。
     */
    private void store(String key, Object value) {
        try {
            // 消息体序列化为 JSON 后存入 Redis，24h(86400s) 自动过期，避免无限增长
            redisUtil.set(key, objectMapper.writeValueAsString(value), 86400);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to store MQ snapshot", ex);
        }
    }

    /**
     * 统一的处理模板：
     *   - 执行业务 action
     *   - 失败时检查重试次数：超过 maxRetries 转死信队列，否则抛 AmqpRejectAndDontRequeueException 让 MQ 重试
     */
    /**
     * 统一的处理模板（两个监听器共用）：执行业务 action，失败则按重试次数决定重试还是转死信。
     *
     * @param businessKey 业务标识（通常是 orderNo）。为什么传：日志/死信里标识"哪条消息"，便于排查。
     * @param queueName   当前消费的队列名。为什么传：用于从 x-death 中匹配本队列的重试次数。
     * @param rawMessage  RabbitMQ 原始消息。为什么传：读取 x-death 头获取已重试次数。
     * @param payload     原始消息体。为什么传：转死信时原样转发，保留完整内容。
     * @param action      具体业务处理逻辑（lambda）。为什么传：把"存快照/记日志"等差异逻辑注入模板，
     *                   复用同一套重试/死信判定，避免重复代码。
     */
    private void process(String businessKey, String queueName, Message rawMessage,
                         Object payload, Runnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            // 从 x-death header 读取该消息在本队列已重试的次数
            int retryCount = extractRetryCount(rawMessage, queueName);
            if (retryCount >= maxRetries) {
                // 达到最大重试次数仍失败 → 转入死信队列，保留证据等待人工处理，不再无谓重试
                log.error("Message {} failed after {} retries, sending to DLQ", businessKey, retryCount, ex);
                orderMessagePublisher.publishToDeadLetter(queueName, payload, retryCount, ex.getMessage());
                return;
            }
            log.warn("Message {} failed on retry {}", businessKey, retryCount + 1, ex);
            // 未达上限 → 抛"拒绝且不重新入队"异常，RabbitMQ 会按死信/重试配置再次投递，实现有限次重试
            throw new AmqpRejectAndDontRequeueException("Message processing failed, routed to retry queue", ex);
        }
    }

    /**
     * 从 x-death header 提取本队列的重试次数。
     *
     * @param rawMessage RabbitMQ 原始消息。为什么传：x-death 重试历史写在它的消息头里。
     * @param queueName  当前队列名。为什么传：x-death 可能有多条（不同队列），只取与当前队列匹配的记录。
     */
    @SuppressWarnings("unchecked")
    private int extractRetryCount(Message rawMessage, String queueName) {
        if (rawMessage == null) return 0;
        // x-death 是 RabbitMQ 死信机制写入的 header，记录每条消息被拒绝/过期的历史（List<Map>）
        Object xDeathHeader = rawMessage.getMessageProperties().getHeaders().get("x-death");
        if (!(xDeathHeader instanceof List)) return 0;
        for (Object item : (List<Object>) xDeathHeader) {
            if (!(item instanceof Map)) continue;
            Map<?, ?> death = (Map<?, ?>) item;
            // 一个队列可能有多条 x-death 记录，只取与当前队列名匹配的那一条
            if (!queueName.equals(String.valueOf(death.get("queue")))) continue;
            Object count = death.get("count");  // 该队列已重试次数
            if (count instanceof Number) return ((Number) count).intValue();
        }
        return 0;
    }
}