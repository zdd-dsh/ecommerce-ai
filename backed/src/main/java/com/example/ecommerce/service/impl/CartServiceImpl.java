package com.example.ecommerce.service.impl;

import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.CartCheckoutRequest;
import com.example.ecommerce.dto.CartItemRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.dto.OrderDetailResponse;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.mapper.CartItemMapper;

import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.UserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductService productService;
    private final OrderService orderService;
    private final UserAddressService userAddressService;




    public CartServiceImpl(CartItemMapper cartItemMapper, ProductService productService, OrderService orderService, UserAddressService userAddressService) {
        this.cartItemMapper = cartItemMapper;
        this.productService = productService;
        this.orderService = orderService;
        this.userAddressService = userAddressService;
    }

    @Override
    public List<CartItemResponse> getCart(Long userId) {
        return cartItemMapper.selectCartByUserId(userId);
    }

    @Transactional
    @Override
    public void addItem(Long userId, CartItemRequest request) {
        //1、参数校验
        validateItemRequest(request);

        //2、查商品并校验状态与库存
        Product product = productService.getById(request.getProductId());
        //新增场景：校验本次传入的数量
        validateStock(product,request.getQuantity());

        //3.1 先查购物车是否已有该商品
        CartItem existing= cartItemMapper.selectByUserAndProduct(userId, request.getProductId());

        if(existing == null){
            //分支1：购物车没有，新增一条记录
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
            cartItemMapper.insert(cartItem);
        }else{
            //分支2：购物车已有，累加数量更新
            int mergedQuantity = existing.getQuantity() + request.getQuantity();
            //校验累加之后总数量不能超过库存
            validateStock(product,mergedQuantity);
            cartItemMapper.updateQuantity(existing.getId(), userId, mergedQuantity);
        }
        // 删除原来末尾多余的 updateQuantity！！！
    }

    @Override
    public void updateItem(Long userId, Long itemId, CartItemRequest request) {
        //校验新修改商品的数量是否合法 request
        if(request == null || request.getQuantity() == null || request.getQuantity() <=0){
            //400参数错误
            throw new BusinessException(Result.BAD_REQUEST_CODE, "购物车要修改的数量有误");
        }
        //查询购物车项，验证存在性（如果购物车中有该商品，数量是修改）
        CartItem cartItem = cartItemMapper.selectById(itemId);
        if(cartItem == null || !userId.equals(cartItem.getUserId())){
            //404 路径或资源不存在
            throw new BusinessException(Result.NOT_FOUND_CODE, "Cart Item does not exist");
        }
        //查询商品信息，校验库存
        Product product = productService.getById(cartItem.getProductId());
        validateStock(product,request.getQuantity());
        //更新购物车中的商品的数量
        cartItemMapper.updateQuantity(itemId, userId, request.getQuantity());

    }
    // CartServiceImpl.java 加
    @Override
    public void removeItem(Long userId, Long itemId) {
        cartItemMapper.deleteByIdAndUserId(itemId ,userId);
    }

    @Override
    public void clear(Long userId) {
        cartItemMapper.deleteByUserId(userId);
    }

    @Transactional
    @Override
    public OrderDetailResponse checkout(Long userId, CartCheckoutRequest request) {
        //1 解析收货地址（优先使用指定地址，否则默认地址）
        UserAddress address = resolveAddress(userId, request);
        //2 查询购物车中的所有商品（调用本类中的方法）
        List<CartItemResponse> cartItems = getCart(userId);
        //3 空购物车的校验
        if (cartItems.isEmpty()) {
            throw new BusinessException(Result.BAD_REQUEST_CODE, "购物车是空不能进行结算");
        }
        //4 构建订单对象
        Order order = new Order();
        order.setUserId(userId);
        order.setAddress(address.getFullAddress());
        order.setPhone(address.getPhone());
        order.setReceiver(address.getReceiver());
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        for(CartItemResponse cartItem:cartItems){
            //创建一个订单项
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            //将该订单项添加到集合中
            orderItems.add(orderItem);
        }

        //6、创建订单 调用订单的业务层
        OrderDetailResponse detailResponse = orderService.save(order, orderItems);
        //7、清空购物车 调用购物车的Dao数据访问层
        cartItemMapper.deleteByUserId(userId);
        //返回结果
        return detailResponse;


    }


    private UserAddress resolveAddress(Long userId, CartCheckoutRequest request){
        //1、用户指定了收货地址
        if(request !=null && request.getAddressId() != null){
            //调用地址的业务层，查询地址
            UserAddress userAddress = userAddressService.getOwnedAddress(userId, request.getAddressId());
            return userAddress;
        }

        //2、使用默认地址
        //调用地址的业务层，查询数据
        //defaultAddress就是查询出来的默认地址
        UserAddress defaultAddress = userAddressService.getDefaultAddress(userId);
        if(defaultAddress != null){
            return defaultAddress;
        }

        //3、无地址可用，抛出异常
        throw new BusinessException(Result.BAD_REQUEST_CODE,"请添加或选择一个地址");
    }


    private void validateStock(Product product, Integer quantity) {
        //校验状态
        if(product==null || product.getStatus()==null || product.getStatus()!=1){
            //Result.NOT_FOUND_CODE 404 路径或资源不存在
            throw new BusinessException(Result.NOT_FOUND_CODE,"商品不存在或者商品状态不对");
        }
        //校验库存
        if(product.getStock()==null || product.getStock() < quantity){
            throw new BusinessException(Result.CONFLICT_CODE,"商品库存异常");
        }

    }

    //校验请求参数完整性
    private void validateItemRequest(CartItemRequest request){
        if(request==null || request.getProductId()==null || request.getQuantity() <=0){
            throw new BusinessException(Result.BAD_REQUEST_CODE,"Product and quantity are required");
        }
    }
}
