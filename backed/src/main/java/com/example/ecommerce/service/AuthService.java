package com.example.ecommerce.service;

import com.example.ecommerce.dto.AuthResponse;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.entity.User;

/*
* 认证服务接口
* 登录 注册 退出
* */
public interface AuthService {
    //登录

    AuthResponse login(String username, String password);



    //注册
    User register(RegisterRequest request);



    //退出
    void logout(String token);
}
