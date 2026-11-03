package com.example.ecommerce.service;

import com.example.ecommerce.dto.CartCheckoutRequest;
import com.example.ecommerce.dto.OrderDetailResponse;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.UserAddress;

import java.util.List;

public interface OrderService {
    /*
     * 根据订单的主键id查询对应的订单
     * @param id 主键id
     * @return 订单
     */
    Order getById(Long id);

    /*
     * 根据订单的主键id查询订单DTO
     * @param id 主键id
     * @return  OrderDetailResponse 订单详情响应DTO类（Order order, List<OrderItem> orderItems）
     */
    OrderDetailResponse getDetailById(Long id);

    /*
     * 根据订单编号查询订单
     * @param orderNo 订单编号 20260416001
     * @return 订单Order
     */
    Order getByOrderNo(String orderNo);
    /*
     * @param userId 用户id
     * @return 集合，存储的是订单
     */
    List<Order> getByUserId(Long userId);

    /*
     * 查询所有的订单
     * @return 集合，存储的是订单
     */
    List<Order> getAll();

    /*
     * 创建订
     * @param order 订单 Order类型
     * @param orderItems 该订单下的商品明细，List类型
     * @return
     */
    OrderDetailResponse save(Order order, List<OrderItem> orderItems);

    /*
     * 修改订单
     * @param order 订单
     */
    void update(Order order);

    /*
     * 删除订单
     * @param id 订单表中的主键id
     */
    void delete(Long id);

    /*
     * 根据订单编号修改订单的状态
     * @param orderNo 订单编号20260416001
     * @param status  订单的状态  状态 0=待支付  1=已支付  2=已完成  3=已取消
     */
    void updateStatusByOrderNo(String orderNo, Integer status);

    OrderDetailResponse createOrderFromCart(Long userId, CartCheckoutRequest request, UserAddress address);


}
