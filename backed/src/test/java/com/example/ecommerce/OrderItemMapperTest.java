

package com.example.ecommerce;

import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.mapper.OrderItemMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class OrderItemMapperTest {

    @Autowired
    private OrderItemMapper orderItemMapper;
    //批量插入
    //批量插入
    @Test
    void test1() {
        List<OrderItem> itemList = new ArrayList<>();

        OrderItem item1 = new OrderItem();
        item1.setOrderId(1L);
        item1.setProductId(1L); //手机
        item1.setProductName("手机1");
        item1.setPrice(new BigDecimal("100"));
        item1.setQuantity(1);
        itemList.add(item1);  //

        OrderItem item2 = new OrderItem();
        item2.setOrderId(1L);
        item2.setProductId(2L); //蓝牙耳机
        item2.setProductName("蓝牙耳机1");
        item2.setPrice(new BigDecimal("200"));
        item2.setQuantity(1);
        itemList.add(item2);  //
        int rows = orderItemMapper.batchInsert(itemList);
        System.out.println("批量插入影响行数：" + rows);
    }

    //查询
    @Test
    void test2() {
        List<OrderItem> itemList = orderItemMapper.selectByOrderId(1L);
        itemList.forEach(System.out::println);
    }
    //插入
    @Test
    void test3() {
        OrderItem item = new OrderItem();
        item.setOrderId(2L);
        item.setProductId(3L);
        item.setProductName("商品C");
        item.setPrice(new BigDecimal("29.90"));
        item.setQuantity(5);
        int rows = orderItemMapper.insert(item);
        System.out.println("插入行数：" + rows);
    }
    //删除
    @Test
    void test4() {
        int rows = orderItemMapper.deleteByOrderId(1L);
        System.out.println("删除行数：" + rows);
    }
}

