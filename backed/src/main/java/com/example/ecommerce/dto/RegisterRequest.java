package com.example.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/*
* 注册请求的DTO
* 包含了注册页面全部字段的验证规则
* */
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min=4, max=20, message = "用户名长度需为4-20位")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "用户名仅支持字母数字和下划线")

    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度需要为8-20位")
    private String password;
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;//重复密码
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50,message = "昵称的长度不能超过50位")
    private String nickname;
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机格式不正确")
    private String phone;


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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
