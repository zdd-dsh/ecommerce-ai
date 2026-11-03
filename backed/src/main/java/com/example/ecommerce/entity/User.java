package com.example.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/*
*
*
*    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    username VARCHAR(50) NOT NULL UNIQUE,
                                    password VARCHAR(100) NOT NULL,
                                    nickname VARCHAR(50),
                                    email VARCHAR(100),
                                    phone VARCHAR(20),
                                    role VARCHAR(20) NOT NULL DEFAULT 'USER',
                                    status INT DEFAULT 1,
                                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

*
*
*
* */
//lombok包下的@Data作用 自动生成getter 和setter方法，equals方法
@Data
public class User {
    //定义User类中的成员变量，对应user表中的字段
    private Long id;                        //用户主键
    private String username;                //用户名（账号登录，唯一）
    /*
    * 密码（BCrupt加密存储）
    *在反序列化时使用
    * */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;//密码
    private String nickname;
    private String email;
    private String phone;
    private String role;
    private Integer status; //1=启用 0-禁用
    //create_time 采用驼峰命名法 createTime
    private Date createTime;
    private Date updateTime;


}

