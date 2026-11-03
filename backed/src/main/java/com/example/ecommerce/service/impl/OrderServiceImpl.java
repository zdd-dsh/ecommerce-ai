package com.example.ecommerce.service.impl;

import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.CartCheckoutRequest;
import com.example.ecommerce.dto.OrderDetailResponse;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.mapper.CartItemMapper;

import com.example.ecommerce.mapper.OrderItemMapper;
import com.example.ecommerce.mapper.OrderMapper;
import com.example.ecommerce.mapper.ProductMapper;
import com.example.ecommerce.messaging.OrderMessagePublisher;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    //引入Dao数据访问层（订单的数据访问层、商品明细的数据访问层、商品的数据访问层）
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final CartItemMapper cartMapper;

    private final RedisUtil redisUtil;

    private final OrderMessagePublisher orderMessagePublisher;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper, ProductMapper productMapper,
                            CartItemMapper cartMapper, RedisUtil redisUtil, OrderMessagePublisher orderMessagePublisher) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.cartMapper = cartMapper;
        this.redisUtil = redisUtil;
        this.orderMessagePublisher = orderMessagePublisher;
    }

    @Override
    public Order getById(Long id) {
        Order order = orderMapper.selectById(id);
        return order;
    }

    @Override
    public OrderDetailResponse getDetailById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(Result.NOT_FOUND_CODE,"order does not exist");
        }
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(id);
        return new OrderDetailResponse(order, orderItems);
    }


    @Override
    public Order getByOrderNo(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        return order;
    }

    @Override
    public List<Order> getByUserId(Long userId) {
        List<Order> orders = orderMapper.selectByUserId(userId);
        return orders;
    }

    @Override
    public List<Order> getAll() {
        List<Order> orders = orderMapper.selectAll();
        return orders;
    }

    @Override
    @Transactional
    public OrderDetailResponse save(Order order, List<OrderItem> orderItems) {
        if (order == null) {
            throw new BusinessException(Result.BAD_REQUEST_CODE,"order payload is required");
        }
        List<OrderItem> saveItems =  orderItems ==null ? Collections.emptyList() : orderItems;
        if(saveItems.isEmpty()){
            throw new BusinessException(Result.BAD_REQUEST_CODE,"order items cannot be empty");
        }

        if(order.getOrderNo() == null || order.getOrderNo().trim().isEmpty()){
            String strUUID = UUID.randomUUID().toString().replace("-","");
            order.setOrderNo(strUUID);
        }

        Order existing = orderMapper.selectByOrderNo(order.getOrderNo());
        if(existing != null){
            log.warn("订单号{}已存在，返回已有订单详情",order.getOrderNo());
            return getDetailById(existing.getId());
        }

        if(order.getStatus() == null){
            order.setStatus(0);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : saveItems) {
            Product product =  validateAndDecreaseStock(item);
            item.setProductName(product.getName());
            item.setProductId(product.getId());

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            clearProductCache(product);
        }
        order.setTotalAmount(totalAmount);

        orderMapper.insert(order);

        //7 把生成的orderId回填到每个订单项中，再批量插入
        for(OrderItem item : saveItems){
            item.setOrderId(order.getId());
        }

        //调用Dao数据访问层，完成批量插入
        orderItemMapper.batchInsert(saveItems);

        OrderDetailResponse resp = new OrderDetailResponse(order, orderItems);
        return resp;
    }


    //定义私有方法：清除商品相关Redis缓存
    private void clearProductCache(Product product){
        try{
            //根据key值清除redis中的相关商品
            redisUtil.delete("product:"+product.getId());
            redisUtil.delete("products:all");

        }catch (Exception e){
            log.warn("Failed to clear product cache:{}", e.getMessage());
        }
    }


    private Product validateAndDecreaseStock(OrderItem item) {
        if(item.getProductId() == null || item.getProductId() == null || item.getQuantity() <0){
            throw new BusinessException(Result.BAD_REQUEST_CODE,"product and quantity is required");
        }
        Product product =  productMapper.selectById(item.getProductId());
        if (product == null || product.getStatus()==null || product.getStatus() !=1){
            throw new BusinessException(Result.NOT_FOUND_CODE, "Product does not exist or is unavailable");
        }
        int updated = productMapper.decreaseStock(item.getProductId(),item.getQuantity());
        if(updated !=1) {
            throw new BusinessException(Result.CONFLICT_CODE, "库存扣减失败"+product.getName());
        }
        return product;
    }


    @Override
    public void update(Order order) {
        orderMapper.update(order);
    }

    @Override
    public void delete(Long id) {
        //调用Dao数据访问层，完成删除的操作
        orderMapper.delete(id);
        orderItemMapper.deleteByOrderId(id);

    }

    @Override
    public void updateStatusByOrderNo(String orderNo, Integer status) {
        orderMapper.updateStatusByOrderNo(orderNo, status);
    }

    @Override
    @Transactional
    public OrderDetailResponse createOrderFromCart(Long userId, CartCheckoutRequest request, UserAddress address) {
        // 用循环逐个查询，替代不存在的批量 selectById
        List<CartItem> cartList = new java.util.ArrayList<>();
        for (Long cartItemId : request.getCartItemIdList()) {
            CartItem cartItem = cartMapper.selectById(cartItemId);
            if (cartItem != null) {
                cartList.add(cartItem);
            }
        }

        if (cartList.isEmpty()) {
            throw new BusinessException(Result.BAD_REQUEST_CODE, "购物车项不存在或已被删除");
        }

        List<OrderItem> orderItems = cartList.stream().map(cart -> {
            OrderItem item = new OrderItem();
            item.setProductId(cart.getProductId());
            item.setQuantity(cart.getQuantity());
            return item;
        }).collect(Collectors.toList());

        Order order = new Order();
        order.setUserId(userId);
        order.setReceiver(address.getReceiver());
        order.setPhone(address.getPhone());
        order.setAddress(address.getFullAddress());

        OrderDetailResponse resp = save(order, orderItems);

        // 用已有的 deleteCheckedCartItems，带 userId 校验，比 deleteById 更安全
        cartMapper.deleteCheckedCartItems(userId, request.getCartItemIdList());

        return resp;
    }

}
