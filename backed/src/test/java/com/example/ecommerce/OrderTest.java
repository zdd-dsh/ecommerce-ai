package com.example.ecommerce;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
public class OrderTest {
    @Autowired
    private OrderMapper orderMapper;
    @Test
    void test1(){
//        Order order = orderMapper.selectById(1L);
//        System.out.println(order);
//        Order order = orderMapper.selectByOrderNo("20260416002");
//        System.out.println(order);


    }
    @Test
    void test2(){
        Order order = new Order();
        order.setUserId(1L);
        order.setOrderNo("202609140001");
        order.setTotalAmount(new BigDecimal("6000.00"));
        order.setStatus(0);
        order.setAddress("山东青岛黄岛区");
        order.setPhone("13866669999");
        order.setReceiver("张三");
        orderMapper.insert(order);
        System.out.println("插入成功");

    }

}
