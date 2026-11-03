package com.example.ecommerce;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.mapper.UserMapper;
import com.example.ecommerce.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserTest1 {

    //声明全局变量 引入userMapper
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;


    @Test
    void test01() {
//       User user = userMapper.selectById(1L);
//       System.out.println(user);
//       User user =  userMapper.selectByUsername("user1");
//        System.out.println(user);
//        List<User> users = userMapper.selectAll();
//        System.out.println(users);
    //        Long userCount = userMapper.countAll();
    //        System.out.println(userCount);
//       创建user对象
//        User user = new User();
//        user.setUsername("xh1");
//        user.setNickname("662");
//        user.setPassword("1234561");
//        user.setEmail("daawdwad@qq1.com");
//        user.setPhone("16662761724");
//        user.setRole("admin");
//        user.setStatus(1);
//        int i =  userMapper.insert(user);
//        System.out.println(i+"数据插入成功");
//        System.out.println(user);

    }
    @Test
    void test02(){
        //将id为6的用户数据进行查询
        User user= userMapper.selectById(4L);
        System.out.println(user);
        //User(id=6, username=小红2, password=123456, nickname=xh2, email=xh1@qq.com, phone=13845678912,
        // role=admin, status=1, createTime=Fri Aug 14 14:46:57 CST 2026, updateTime=Fri Aug 14 14:46:57 CST 2026)
        user.setUsername("dd");
        user.setPassword("12444");
        user.setEmail("564546546");

        //调用修改用户的方法进行数据的修改
        int i=userMapper.update(user);
        System.out.println(i);  //1
        System.out.println(user);
    }
    @Test
    void test03(){
//         int i = userMapper.updatePassword(2l,"222222");
//         System.out.println("密码修改成功");
//        User user = userMapper.selectById(2L);
//        System.out.println(user);
        userMapper.delete(7L);
        System.out.println("删除成功");
    }
    @Test
    void test04(){

    }
    @Test
    void test05(){
        User user =new User();
        System.out.println(user);

        user.setUsername("lisi1234");
        user.setEmail("lisi@qq.com");
        user.setId(1L);
        user.setPassword("123456");
        userService.update(user);
    }
}
