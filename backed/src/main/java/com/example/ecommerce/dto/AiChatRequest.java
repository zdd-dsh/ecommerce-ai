package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * AI 对话请求体。
 */
public class AiChatRequest {

    @NotBlank(message = "消息不能为空")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
