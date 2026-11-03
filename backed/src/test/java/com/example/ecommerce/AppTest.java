package com.example.ecommerce;


import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.entity.User;

import java.util.Date;

/**
 * Unit test for simple App.
 */
public class AppTest{
    public static void main(String[] args) {
        User user=new User();
        System.out.println(user);//com.example.ecommerce.entity.User@2aae9190
        //User{id=null, username='null', password='null', nickname='null', email='null', phone='null', role='null', status
        user.setPassword("123456");
        System.out.println(user.getPassword());
    }

}
