package com.example.ecommerce;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.mapper.UserMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

public class UserTest {
    public static void main(String[] args) {
      ApplicationContext context = SpringApplication.run(UserTest.class);
      UserMapper mapper = context.getBean(UserMapper.class);

      User user = mapper.selectById(1L);
      System.out.println(user);
    }
}
