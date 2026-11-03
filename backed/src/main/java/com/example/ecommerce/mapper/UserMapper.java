package com.example.ecommerce.mapper;

import com.example.ecommerce.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/*
*
*
* MyBatis框架
* 用户数据访问层 --接口 方法都是抽象方法，没有方法体
*
* */
@Mapper
public interface UserMapper {
    /*
    * 根据ID查询用户
    @param id    用户ID，主键
    @return      返回User对象（用户信息）
    * */
    User selectById(long id);

    /*
     *根据用户名查询用户
     *  @param username    用户ID，主键
        @return      返回User对象（用户信息）
     */
    User selectByUsername(String username);
    /*
    * 查询所有用户
    * @return 返回List<User>集合，集合中存储的user信息
    * */
    List<User> selectAll();

    /*
    * 统计用户的总数
    * @return long类型 用户总数
    * */
    long countAll();
    //插入一个新用户
    int insert(User user);
    //更新用户信息
    int update(User user);

    //单独更新密码
    //
    int updatePassword(@Param("id") long id, @Param("password")  String password);
    //删除用户
    int delete(Long id);


    void updateNickname(@Param("id") Long userId, @Param("nickname") String nickname);

    void updateEmail(@Param("id") Long userId, @Param("email") String email);

    void updatePhone(@Param("id") Long userId, @Param("phone") String phone);

    //根据手机号查询用户，用于手机号唯一性校验
    //根据手机号查询用户，用于手机号唯一性校验
    User selectByPhone(String phone);
}

