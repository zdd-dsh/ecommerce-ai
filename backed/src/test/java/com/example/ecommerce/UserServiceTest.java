package com.example.ecommerce;

import com.example.ecommerce.dto.AuthResponse;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.mapper.UserMapper;
import com.example.ecommerce.service.AuthService;
import com.example.ecommerce.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/*
* 测试用户的业务层
* */
@SpringBootTest
public class UserServiceTest {

    //引入数据持久层
    @Autowired
    private UserMapper userMapper;
    //引入业务层
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;


    @Test
    void test01(){
    User user = userMapper.selectById(1L);
    System.out.println(user);
    }
    @Test
    void test02(){
        User user = userService.getByUsername("dd");
        System.out.println(user);
    }
    @Test
    void test03(){
        List<User> users = userService.getAll();
        System.out.println(users);
    }
    @Test
    void test04(){
//        User user=null;
//        userService.save(user);
        User user= new User();
        user.setUsername("rrdr_123");//不符合
        user.setPassword("123456");
        user.setNickname("ee");
        user.setEmail("zsf@qq.com");
        user.setPhone("13800138001");
        userService.save(user);
    }
    @Test
    void test05(){
        User user = new User();
        System.out.println(user);
        user.setUsername("rreerr_123");
        user.setEmail("we@qq.cpm");
        user.setId(1L);

        userService.update(user);
    }
    @Test
    void test06() {
        //id为6的用户数据, (username, 密码, 邮箱)
        User user = new User();
        user.setUsername("xiaoli3");
        user.setPassword("1231316");
        user.setId(2L);
        user.setEmail("xiaolid@qq.com");
        userService.update(user);
        System.out.println("修改成功");
    }
    @Test
    void test07() {
        userService.updatePassword(4L, "888888");
        System.out.println("密码修改成功");
    }
    @Test
    void test08() {
        userService.delete(10L);
        System.out.println("删除用户成功");
    }
    @Test
    void test09() {
//        User user =new User();
//        user.setUsername("zs_666");
//        user.setPassword("123456");
//        user.setNickname("zs666");
//        user.setEmail("zs@qq.com");
//        user.setPhone("13536253265");
        //status和role可以不用设置值
//        userService.save(user);
        AuthResponse auth = authService.login("zs_666", "123456");
        System.out.println(auth);
        //测试退出
        authService.logout(auth.getToken());
        System.out.println(auth);
    }
    @Test
    void test10() {
//        1
//        RegisterRequest request = null;
//        User user = authService.register(request);

//        2
//        RegisterRequest request = new RegisterRequest();
//        request.setPassword("123456");
//        request.setConfirmPassword("654321");
//        authService.register(request);
//        3
//        RegisterRequest request = new RegisterRequest();
//        request.setPassword("123456");
//        request.setConfirmPassword("123456");
//        request.setUsername("zs_666");
//        authService.register(request);
        RegisterRequest request = new RegisterRequest();
        request.setPassword("123456");
        request.setConfirmPassword("123456");
        request.setUsername("ls_1");
        request.setNickname("l1");
        request.setEmail("ls1@qq.com");
        request.setPhone("13800138001");
        User user = authService.register(request);
        System.out.println(user);
    }
    @Test
    void test11() {
        Long userId = 10L;
        String newNickname = "wwww1";
        User user = userService.updateNickname(userId, newNickname);
        System.out.println("修改后用户：" + user);
        System.out.println("修改后昵称：" + user.getNickname());
    }
    @Test
    void test12() {
        // 传入用户id、新手机号
        Long userId = 2L;
        String newPhone = "13900139000";
        User user = userService.updatePhone(userId, newPhone);
        System.out.println("修改完成后的用户信息：" + user);
        System.out.println("修改后的手机号：" + user.getPhone());
    }

}
