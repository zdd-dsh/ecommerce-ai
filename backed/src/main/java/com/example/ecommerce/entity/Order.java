package com.example.ecommerce.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/*
 * 实体类 -- 对应数据库中的order  订单表
 */
@Data
public class Order {
    private Long id;            //主键id
    private Long userId;        //下单用户id
    private String orderNo;     //订单号(业务编号 UUID生成)
    private BigDecimal totalAmount; //总金额
    private Integer status;     //状态 0=待支付  1=已支付  2=已完成  3=已取消
    private String address;     //收货地址
    private String phone;       //联系电话
    private String receiver;    //收货人
    private Date createTime;    //创建时间
    private Date updateTime;    //修改时间

    //目标状态（仅用于更新请求，非数据库字段）
    private transient Integer targetStatus;
}

