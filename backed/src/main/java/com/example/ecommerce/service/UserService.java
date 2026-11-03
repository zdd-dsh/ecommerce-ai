package com.example.ecommerce.service;


import com.example.ecommerce.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

//Service业务类

public interface UserService {
    /*
    * 根据主键id的值获取用户的信息
    * */
    User getById(Long id);

    /*
    * 根据用户名获取用户信息
    * */
    User getByUsername(String username);
    //获取所有用户信息
    List<User> getAll();
    //保存用户信息
    void save(User user);

    //修改用户信息
    void update(User user);

    //根据密码修改用户信息
    void updatePassword(Long id, String encodePassword);

    //删除用户信息
    void delete(Long id);

    //密码编码加密
    String encodeIfNecessary(String rawPassword);
    //修改用户昵称
    User updateNickname(Long Id, String nickname);
    User updateEmail(Long Id, String email);
    User updatePhone(Long Id, String phone);

}
