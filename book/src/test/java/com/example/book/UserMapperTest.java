package com.example.book;

import com.example.book.entity.User;
import com.example.book.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    // 测试新增用户
    @Test
    void testAdd() {
        User user = new User();
        user.setName("张三");
        user.setAge(20);
        int result = userMapper.insert(user);
        System.out.println("新增结果：" + result + "，自增id：" + user.getId());
    }

    // 测试查询全部
    @Test
    void testList() {
        List<User> list = userMapper.selectList(null);
        list.forEach(System.out::println);
    }

    // 测试根据id查询
    @Test
    void testGetById() {
        User user = userMapper.selectById(1L);
        System.out.println(user);
    }
}
