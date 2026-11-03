package com.example.ecommerce.dto;

import com.example.ecommerce.entity.User;

/*
*登录 注册成功以后的响应的DTO
* 该类包括token（认证令牌） 和user（用户信息）
*
*
 */
public class AuthResponse {
    //用户输入密码账号登陆成功后，会生成一串token，和用户信息一起返回浏览器
    private String token;
    private User user;
    /*
    * 工厂方法，快速创建响应对象AuthResponse
    *
    * */
    public static AuthResponse of(String token, User user) {
        //创建AuthResponse
        AuthResponse authResponse = new AuthResponse();
        //将参数中的token和user给成员变量赋值
        authResponse.setToken(token);
        authResponse.setUser(user);
        return authResponse;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
