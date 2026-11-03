package com.example.ecommerce.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CartItem implements Serializable {
    private Long id;         //主键id
    private Long userId;     //用户id
    private Long productId;  //商品id
    private Integer quantity;//购买数量
    private Date createTime; //创建时间
    private Date updateTime; //修改时间
}
