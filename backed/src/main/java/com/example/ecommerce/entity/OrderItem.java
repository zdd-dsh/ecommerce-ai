package com.example.ecommerce.entity;
import lombok.Data;
import java.math.BigDecimal;

/*
 * 实体类 对应数据库中的order_item表
 * Order实体类  OrderItem实体类 两个是一对多的关系 一个订单包含多个订单项
 */
@Data
public class OrderItem {
    private Long id;            //主键id
    private Long orderId;       //所属订单id
    private Long productId;     //商品id
    private String productName; //商品名称
    private BigDecimal price;   //商品单价
    private Integer quantity;   //商品数量
}
