package com.example.ecommerce.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
@Data
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String image;
    private Integer categoryId;
    private Integer merchantId;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
