package com.example.ecommerce;

import com.example.ecommerce.dto.OrderDetailResponse;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class OrderServiceTest {
    @Autowired
    private OrderService orderService;
    @Test
    void test01() {
        Order order = orderService.getById(1L);
        System.out.println(order);
    }
    @Test
    void test02() {
        OrderDetailResponse orderDetailResponse = orderService.getDetailById(1L);
        System.out.println(orderDetailResponse);
    }
    @Test
    void test09(){
        Order order = new Order();
        order.setUserId(1L); //模拟 用户id按照实际登录的用户来设置
        order.setReceiver("小红"); //收货人
        order.setPhone("13865653232"); //电话
        order.setAddress("山东省青岛"); //地址

        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        OrderItem item1=new OrderItem();
        item1.setProductId(11L); //购买的第一个商品 模拟
        item1.setProductName("平板电脑");
        item1.setPrice(new BigDecimal("2499.0"));
        item1.setQuantity(1);

        OrderItem item2=new OrderItem();
        item2.setProductId(3L);
        item2.setProductName("机械键盘");
        item2.setPrice(new BigDecimal("399.0"));
        item2.setQuantity(3);

        orderItems.add(item1);
        orderItems.add(item2);

        OrderDetailResponse resp = orderService.save(order, orderItems);

        System.out.println(resp);
    }

}
