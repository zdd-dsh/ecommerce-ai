package com.example.ecommerce.controller;

import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.dto.AiChatRequest;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.mapper.ProductMapper;
import com.example.ecommerce.service.impl.ProductServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Tag(name = "AI 接口", description = "电商导购流式对话")
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final String SYSTEM_PROMPT =
            "你是电商导购，只基于平台商品回答，不知道就说不知道。"
                    + "回答时不要暴露类目编号、商品 ID 等内部字段，用自然语言描述商品。";

    private static final int MAX_PRODUCTS = 20;
    private static final int ON_SALE_STATUS = 1;

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final ProductMapper productMapper;

    public AiController(ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
                        ProductMapper productMapper) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
        this.productMapper = productMapper;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@Valid @RequestBody AiChatRequest request) {
        ChatClient chatClient = requireChatClient();
        SseEmitter emitter = new SseEmitter(60_000L);

        chatClient.prompt()
                .system(buildSystemPrompt())
                .user(request.getMessage())
                .stream()
                .content()
                .subscribe(
                        chunk -> sendChunk(emitter, chunk),
                        emitter::completeWithError,
                        emitter::complete
                );

        return emitter;
    }

    private ChatClient requireChatClient() {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            throw new BusinessException("AI 对话模型未配置");
        }
        return builder.build();
    }

    private String buildSystemPrompt() {
        String catalog = formatOnSaleProducts(loadOnSaleProducts());
        if (catalog.isEmpty()) {
            return SYSTEM_PROMPT;
        }
        return SYSTEM_PROMPT + "\n\n平台在售商品：\n" + catalog;
    }

    private List<Product> loadOnSaleProducts() {
        List<Product> products = productMapper.selectAll();
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
                .filter(product -> product != null && Objects.equals(product.getStatus(), ON_SALE_STATUS))
                .limit(MAX_PRODUCTS)
                .collect(Collectors.toList());
    }

    private String formatOnSaleProducts(List<Product> products) {
        StringBuilder catalog = new StringBuilder();
        for (Product product : products) {
            String categoryName = ProductServiceImpl.CATEGORY_LABELS
                    .getOrDefault(product.getCategoryId(), "其他");
            catalog.append("- ").append(product.getName())
                    .append("，价格 ").append(product.getPrice()).append(" 元")
                    .append("，库存 ").append(product.getStock()).append(" 件")
                    .append("，类目：").append(categoryName)
                    .append('\n');
        }
        return catalog.toString();
    }


    private void sendChunk(SseEmitter emitter, String chunk) {
        try {
            emitter.send(SseEmitter.event().data(chunk));
        } catch (IOException ex) {
            emitter.completeWithError(ex);
        }
    }
}
