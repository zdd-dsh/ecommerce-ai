package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

/*
* 登录请求的DTO
* 对登录页面的用户名和密码做验证
* */
public class AuthLoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
