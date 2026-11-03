package com.example.ecommerce.dto;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import java.util.List;

/**
 * 订单详情返回DTO
 */
public class OrderDetailResponse {
    private Order order;
    private List<OrderItem> orderItems;

    // 无参构造
    public OrderDetailResponse() {
    }

    // 全参构造
    public OrderDetailResponse(Order order, List<OrderItem> orderItems) {
        this.order = order;
        this.orderItems = orderItems;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    // toString
    @Override
    public String toString() {
        return "OrderDetailResponse{" +
                "order=" + order +
                ", orderItems=" + orderItems +
                '}';
    }
}
