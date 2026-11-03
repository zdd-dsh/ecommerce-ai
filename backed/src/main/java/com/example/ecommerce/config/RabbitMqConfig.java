package com.example.ecommerce.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// =============================================================================
// 【教学】RabbitMQ 消息队列配置类
// =============================================================================
//
// 一、消息队列（Message Queue）是什么？
// ---------------------------------
// 消息队列是一种异步通信机制，允许应用程序之间通过"消息"进行交互。
// 生产者（Producer）将消息发送到队列，消费者（Consumer）从队列中取出消息处理。
//
// 二、RabbitMQ 是什么？
// ---------------------------------
// RabbitMQ 是最流行的开源消息代理（Message Broker），实现了 AMQP 协议。
// 核心概念：
//   - Producer（生产者）：发送消息的应用
//   - Exchange（交换机）：接收消息并根据规则路由到队列
//   - Queue（队列）：存储消息的缓冲区
//   - Binding（绑定）：连接 Exchange 和 Queue 的规则
//   - Consumer（消费者）：接收并处理消息的应用
//   - Routing Key（路由键）：消息的"地址标签"，Exchange 根据它决定发往哪个 Queue
//
// 三、为什么需要消息队列？（面试高频考点）
// ---------------------------------
// 1. 异步处理：用户下单后，不需要等待库存扣减、通知发送等操作完成，直接返回
// 2. 解耦：订单服务和库存服务通过消息通信，互不依赖对方的实现
// 3. 削峰：高并发时消息先在队列中排队，消费者按自己的速度处理
// 4. 可靠性：消息持久化后即使服务重启也不会丢失
//
// 四、本项目中的消息流
// ---------------------------------
// 订单创建 → Exchange → order-created-queue → 消费者处理（扣减库存、发送通知）
// 支付状态变更 → Exchange → payment-status-queue → 消费者处理（更新订单状态）
//
// 五、死信队列（Dead Letter Queue）模式详解（面试高频考点）
// ---------------------------------
// 当消息处理失败时，我们不希望消息直接丢弃，而是：
//   1. 第一次失败 → 消息进入"重试队列"（Retry Queue）
//   2. 重试队列有 TTL（存活时间），消息等待一段时间后自动"死信"回主队列
//   3. 再次失败 → 再次进入重试队列
//   4. 超过最大重试次数 → 消息进入"死信队列"（DLQ），人工介入处理
//
// 这就是"重试 + 死信"模式，保证消息不丢失、不无限重试。
//
// 六、消息流图示
// ---------------------------------
// [生产者] --> [Event Exchange] --> [主队列] --> [消费者]
//                                  ↓ (失败)
//                             [Retry Exchange] --> [重试队列 (TTL)] --> (TTL到期，死信)
//                                  ↓ (死信)
//                             [Event Exchange] --> [主队列] (重试)
//                                  ↓ (多次失败)
//                             [DLQ Exchange] --> [死信队列] --> [人工处理]
// =============================================================================

@Configuration
public class RabbitMqConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqConfig.class);

    // =========================================================================
    // 【教学】交换机（Exchange）定义
    // =========================================================================

    /**
     * 主事件交换机 —— 所有业务事件的入口。
     *
     * @Value("${ecommerce.rabbit.exchange}") 从 application.yml 中读取交换机名称。
     *
     * DirectExchange 参数说明：
     *   - 第一个参数：交换机名称
     *   - 第二个参数 true：durable（持久化），RabbitMQ 重启后交换机不会丢失
     *   - 第三个参数 false：autoDelete（不自动删除），即使没有队列绑定也不会自动删除
     *
     * @param exchangeName 交换机名称（从配置文件读取）
     * @return 交换机实例
     */
    @Bean
    public DirectExchange ecommerceEventExchange(@Value("${ecommerce.rabbit.exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    /**
     * 重试交换机 —— 处理失败消息的重试路由。
     * 消费失败的消息会被路由到重试队列，等待一段时间后重新投递。
     *
     * @param exchangeName 重试交换机名称
     * @return 交换机实例
     */
    @Bean
    public DirectExchange ecommerceRetryExchange
    (@Value("${ecommerce.rabbit.retry-exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    /**
     * 死信交换机 —— 最终兜底，存放多次重试仍失败的消息。
     * 这些消息需要人工介入处理（如修复数据、联系用户等）。
     *
     * @param exchangeName 死信交换机名称
     * @return 交换机实例
     */
    @Bean
    public DirectExchange ecommerceDeadLetterExchange
    (@Value("${ecommerce.rabbit.dead-letter-exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    // =========================================================================
    // 【教学】队列（Queue）定义
    // =========================================================================

    /**
     * 订单创建事件队列 —— 接收"订单已创建"消息。
     *
     * 【教学】QueueBuilder.durable() 创建持久化队列
     * durable = true 表示队列会持久化到磁盘，RabbitMQ 重启后队列和消息不会丢失。
     *
     * 【教学】x-dead-letter-exchange 和 x-dead-letter-routing-key
     * 这两个参数定义了"死信路由"：
     *   - 当消息被拒绝（reject/nack）且 requeue=false 时
     *   - 或消息 TTL 过期时
     *   - 或队列达到最大长度时
     * 消息会被发送到指定的死信交换机，使用指定的路由键。
     *
     * @param queueName 队列名称
     * @param retryExchangeName 重试交换机名称（死信目标）
     * @return 队列实例
     */
    @Bean
    public Queue orderCreatedQueue(@Value("${ecommerce.rabbit.order-created-queue}") String queueName,
                                   @Value("${ecommerce.rabbit.retry-exchange}") String retryExchangeName) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", retryExchangeName)
                .withArgument("x-dead-letter-routing-key", retryRoutingKey(queueName))
                .build();
    }

    /**
     * 支付状态事件队列 —— 接收"支付状态变更"消息。
     * 配置与订单创建队列相同，也设置了死信路由。
     *
     * @param queueName 队列名称
     * @param retryExchangeName 重试交换机名称
     * @return 队列实例
     */
    @Bean
    public Queue paymentStatusQueue(@Value("${ecommerce.rabbit.payment-status-queue}") String queueName,
                                    @Value("${ecommerce.rabbit.retry-exchange}") String retryExchangeName) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", retryExchangeName)
                .withArgument("x-dead-letter-routing-key", retryRoutingKey(queueName))
                .build();
    }

    /**
     * 订单创建重试队列 —— 暂存消费失败的消息，等待重新投递。
     *
     * 【教学】重试队列的核心机制：
     *   1. 消费失败的消息被路由到这个队列
     *   2. 队列设置了 x-message-ttl（消息存活时间），消息在此等待指定毫秒
     *   3. TTL 到期后，消息"死信"到 x-dead-letter-exchange（主交换机）
     *   4. 主交换机再将消息路由回主队列，消费者再次处理
     *
     * 这形成了一个"延迟重试"的效果：失败 → 等一会儿 → 重试。
     *
     * @param queueName 原始队列名称（用于生成重试队列名）
     * @param exchangeName 主交换机名称（TTL 到期后消息回到这里）
     * @param retryTtlMs 重试等待时间（毫秒）
     * @return 重试队列实例
     */
    @Bean
    public Queue orderCreatedRetryQueue(@Value("${ecommerce.rabbit.order-created-queue}") String queueName,
                                        @Value("${ecommerce.rabbit.exchange}") String exchangeName,
                                        @Value("${ecommerce.rabbit.retry-ttl-ms}") long retryTtlMs) {
        return QueueBuilder.durable(retryQueueName(queueName))
                .withArgument("x-message-ttl", retryTtlMs)
                .withArgument("x-dead-letter-exchange", exchangeName)
                .withArgument("x-dead-letter-routing-key", queueName)
                .build();
    }

    /**
     * 支付状态重试队列 —— 暂存消费失败的支付状态消息。
     *
     * @param queueName 原始队列名称
     * @param exchangeName 主交换机名称
     * @param retryTtlMs 重试等待时间
     * @return 重试队列实例
     */
    @Bean
    public Queue paymentStatusRetryQueue(@Value("${ecommerce.rabbit.payment-status-queue}") String queueName,
                                         @Value("${ecommerce.rabbit.exchange}") String exchangeName,
                                         @Value("${ecommerce.rabbit.retry-ttl-ms}") long retryTtlMs) {
        return QueueBuilder.durable(retryQueueName(queueName))
                .withArgument("x-message-ttl", retryTtlMs)
                .withArgument("x-dead-letter-exchange", exchangeName)
                .withArgument("x-dead-letter-routing-key", queueName)
                .build();
    }

    /**
     * 订单创建死信队列 —— 存放多次重试仍失败的消息。
     * 这些消息需要人工介入处理。
     *
     * @param queueName 原始队列名称
     * @return 死信队列实例
     */
    @Bean
    public Queue orderCreatedDeadLetterQueue(@Value("${ecommerce.rabbit.order-created-queue}") String queueName) {
        return new Queue(deadLetterQueueName(queueName), true);
    }

    /**
     * 支付状态死信队列 —— 存放多次重试仍失败的支付消息。
     *
     * @param queueName 原始队列名称
     * @return 死信队列实例
     */
    @Bean
    public Queue paymentStatusDeadLetterQueue(@Value("${ecommerce.rabbit.payment-status-queue}") String queueName) {
        return new Queue(deadLetterQueueName(queueName), true);
    }

    // =========================================================================
    // 【教学】绑定（Binding）定义
    // =========================================================================

    /**
     * 【教学】Binding 的作用
     * Binding 将 Queue 和 Exchange 连接起来，并指定路由键（Routing Key）。
     * 当消息发送到 Exchange 时，Exchange 根据消息的 Routing Key 查找匹配的 Binding，
     * 将消息投递到对应的 Queue。
     *
     * BindingBuilder.bind(queue).to(exchange).with(routingKey) 的语义：
     *   "将 queue 绑定到 exchange，使用 routingKey 作为匹配规则"
     *
     * DirectExchange 的匹配规则：消息的 Routing Key 必须完全等于 Binding 的 Routing Key。
     */

    /**
     * 订单创建队列绑定到主事件交换机。
     * 路由键 = 队列名称（这是一种约定，便于管理）。
     */
    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue,
                                       DirectExchange ecommerceEventExchange,
                                       @Value("${ecommerce.rabbit.order-created-queue}") String routingKey) {
        return BindingBuilder.bind(orderCreatedQueue).to(ecommerceEventExchange).with(routingKey);
    }

    /**
     * 支付状态队列绑定到主事件交换机。
     */
    @Bean
    public Binding paymentStatusBinding(Queue paymentStatusQueue,
                                        DirectExchange ecommerceEventExchange,
                                        @Value("${ecommerce.rabbit.payment-status-queue}") String routingKey) {
        return BindingBuilder.bind(paymentStatusQueue).to(ecommerceEventExchange).with(routingKey);
    }

    /**
     * 订单创建重试队列绑定到重试交换机。
     * 路由键为 "原队列名.retry"。
     */
    @Bean
    public Binding orderCreatedRetryBinding(Queue orderCreatedRetryQueue,
                                            DirectExchange ecommerceRetryExchange,
                                            @Value("${ecommerce.rabbit.order-created-queue}") String queueName) {
        return BindingBuilder.bind(orderCreatedRetryQueue).to(ecommerceRetryExchange)
                .with(retryRoutingKey(queueName));
    }

    /**
     * 支付状态重试队列绑定到重试交换机。
     */
    @Bean
    public Binding paymentStatusRetryBinding(Queue paymentStatusRetryQueue,
                                             DirectExchange ecommerceRetryExchange,
                                             @Value("${ecommerce.rabbit.payment-status-queue}") String queueName) {
        return BindingBuilder.bind(paymentStatusRetryQueue).to(ecommerceRetryExchange)
                .with(retryRoutingKey(queueName));
    }

    /**
     * 订单创建死信队列绑定到死信交换机。
     * 路由键为 "原队列名.dlq"。
     */
    @Bean
    public Binding orderCreatedDeadLetterBinding(Queue orderCreatedDeadLetterQueue,
                                                 DirectExchange ecommerceDeadLetterExchange,
                                                 @Value("${ecommerce.rabbit.order-created-queue}") String queueName) {
        return BindingBuilder.bind(orderCreatedDeadLetterQueue).
                to(ecommerceDeadLetterExchange).with(deadLetterRoutingKey(queueName));
    }

    /**
     * 支付状态死信队列绑定到死信交换机。
     */
    @Bean
    public Binding paymentStatusDeadLetterBinding(Queue paymentStatusDeadLetterQueue,
                                                  DirectExchange ecommerceDeadLetterExchange,
                                                  @Value("${ecommerce.rabbit.payment-status-queue}") String queueName) {
        return BindingBuilder.bind(paymentStatusDeadLetterQueue).to(ecommerceDeadLetterExchange)
                .with(deadLetterRoutingKey(queueName));
    }

    // =========================================================================
    // 【教学】消息转换器和模板
    // =========================================================================

    /**
     * 【教学】消息转换器（MessageConverter）
     * RabbitMQ 传输的是字节数据，Java 对象需要序列化后才能发送。
     * Jackson2JsonMessageConverter 使用 Jackson 将对象序列化为 JSON 字符串。
     *
     * 优点：
     *   1. 可读性强：在 RabbitMQ 管理界面可以直接查看消息内容
     *   2. 跨语言：其他语言（如 Python、Node.js）可以直接解析 JSON
     *   3. 兼容性好：新增/删除字段不会导致反序列化失败
     *
     * @return JSON 消息转换器
     */
    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 【教学】RabbitTemplate —— 消息发送模板
     * RabbitTemplate 是 Spring AMQP 提供的消息发送工具类，类似于 RedisTemplate。
     * 它封装了与 RabbitMQ 的连接管理、消息序列化、确认机制等。
     *
     * 配置要点：
     *   1. setMessageConverter：使用 JSON 转换器（而非默认的 JDK 序列化）
     *   2. setMandatory(true)：消息无法路由到任何队列时，返回给生产者（而非静默丢弃）
     *   3. setBeforePublishPostProcessors：在发送前将消息标记为持久化（PERSISTENT）
     *   4. setConfirmCallback：Publisher Confirm 机制，确认消息是否到达 Broker
     *   5. setReturnsCallback：Returned Message 机制，确认消息是否被路由到队列
     *
     * 【教学】Publisher Confirm vs Publisher Return
     *   - Confirm：消息到达 Broker（RabbitMQ 服务器）时触发，不管是否路由成功
     *   - Return：消息无法路由到任何队列时触发（如路由键不匹配）
     *
     * @param connectionFactory RabbitMQ 连接工厂
     * @param messageConverter 消息转换器
     * @return RabbitTemplate 实例
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);

        // mandatory=true：如果消息无法路由到队列，Broker 会将消息返回给生产者
        rabbitTemplate.setMandatory(true);

        // 【教学】消息后处理器：在发送前将所有消息标记为持久化
        // PERSISTENT 模式告诉 Broker 将消息写入磁盘，即使 RabbitMQ 重启消息也不会丢失
        // 相对的是 TRANSIENT（非持久化），只存在内存中，重启后丢失
        rabbitTemplate.setBeforePublishPostProcessors(message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });

        // 【教学】Publisher Confirm 回调
        // 当消息成功到达 Broker 时，ack=true
        // 当消息发送失败时，ack=false，cause 包含失败原因
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("RabbitMQ publisher confirm success: {}",
                        correlationData == null ? "-" : correlationData.getId());
                return;
            }
            log.error("RabbitMQ publisher confirm failed: {}, cause={}",
                    correlationData == null ? "-" : correlationData.getId(), cause);
        });

        // 【教学】Publisher Return 回调
        // 当消息无法路由到任何队列时触发（如路由键错误、队列不存在）
        rabbitTemplate.setReturnsCallback(returned ->
                log.error("RabbitMQ returned message: exchange={}, routingKey={}, replyCode={}, replyText={}",
                        returned.getExchange(), returned.getRoutingKey(),
                        returned.getReplyCode(), returned.getReplyText()));

        return rabbitTemplate;
    }

    /**
     * 【教学】监听器容器工厂（Listener Container Factory）
     * SimpleRabbitListenerContainerFactory 用于创建消息监听器容器。
     * 监听器容器负责：
     *   1. 管理与 RabbitMQ 的连接
     *   2. 从队列中拉取消息
     *   3. 反序列化消息
     *   4. 调用 @RabbitListener 注解的方法处理消息
     *   5. 处理确认（ACK）和拒绝（NACK）
     *
     * defaultRequeueRejected(false)：当消息处理失败时，不将消息重新放回原队列。
     * 因为我们有重试队列机制，失败的消息会通过死信路由进入重试队列。
     * 如果设为 true，消息会无限重试，可能导致"毒消息"卡住队列。
     *
     * @param configurer Spring Boot 的自动配置器
     * @param connectionFactory RabbitMQ 连接工厂
     * @param messageConverter 消息转换器
     * @return 监听器容器工厂
     */
    @Bean(name = "rabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    /**
     * 【教学】RabbitAdmin —— RabbitMQ 管理工具
     * RabbitAdmin 负责在应用启动时自动声明 Exchange、Queue、Binding。
     * 这就是为什么我们定义了那么多 @Bean（Exchange、Queue、Binding）——
     * RabbitAdmin 会自动检测容器中的这些 Bean 并在 RabbitMQ 中创建它们。
     *
     * setAutoStartup(rabbitEnabled)：根据配置决定是否启用 RabbitMQ。
     * 在测试环境中可以设置 rabbitEnabled=false，跳过 RabbitMQ 的初始化。
     *
     * @param connectionFactory 连接工厂
     * @param rabbitEnabled 是否启用（从配置文件读取）
     * @return RabbitAdmin 实例
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory,
                                   @Value("${ecommerce.rabbit.enabled:true}") boolean rabbitEnabled) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(rabbitEnabled);
        return rabbitAdmin;
    }

    // =========================================================================
    // 【教学】辅助方法：队列和路由键命名规则
    // =========================================================================

    /**
     * 生成重试队列名称：在原队列名后加 ".retry" 后缀。
     * 例如：order-created → order-created.retry
     */
    private String retryQueueName(String queueName) {
        return queueName + ".retry";
    }

    /**
     * 生成重试路由键：在原路由键后加 ".retry" 后缀。
     */
    private String retryRoutingKey(String routingKey) {
        return routingKey + ".retry";
    }

    /**
     * 生成死信队列名称：在原队列名后加 ".dlq" 后缀。
     * DLQ = Dead Letter Queue（死信队列）
     */
    private String deadLetterQueueName(String queueName) {
        return queueName + ".dlq";
    }

    /**
     * 生成死信路由键：在原路由键后加 ".dlq" 后缀。
     */
    private String deadLetterRoutingKey(String routingKey) {
        return routingKey + ".dlq";
    }
}
