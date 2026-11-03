package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.CartCheckoutRequest;
import com.example.ecommerce.dto.CartItemRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.dto.OrderDetailResponse;
import com.example.ecommerce.security.CustomUserDetails;
import com.example.ecommerce.security.SecurityUtils;
import com.example.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="购物车接口", description = "购物车的查询和添加等功能")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Log log = LogFactory.getLog(CartController.class);
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Result<List<CartItemResponse>> getCart(Authentication authentication){
        CustomUserDetails user = SecurityUtils.currentUser(authentication);
        List<CartItemResponse> cartList=cartService.getCart(user.getId());
        return Result.success(cartList);
    }

    @PostMapping("/items")
    public Result<Void> addItem(@RequestBody CartItemRequest request, Authentication authentication){
        CustomUserDetails user =SecurityUtils.currentUser(authentication);
        cartService.addItem(user.getId(), request);
        return Result.success();
    }

    // =========加上 @PutMapping("/items/{id}") =========
    @PutMapping("/items/{id}")
    public Result<Void> updateItem(@PathVariable Long id, @RequestBody CartItemRequest request, Authentication authentication){
        CustomUserDetails user =SecurityUtils.currentUser(authentication);
        System.out.println("id=="+id);
        System.out.println("request=="+request);
        cartService.updateItem(user.getId(), id, request);
        return Result.success();
    }
    // 删除单个购物车项
    @DeleteMapping("/items/{id}")
    public Result<Void> removeItem(@PathVariable Long id, Authentication authentication){
        CustomUserDetails user = SecurityUtils.currentUser(authentication);
        cartService.removeItem(user.getId(), id);
        return Result.success();
    }
    @DeleteMapping
    public Result<Void> clear(Authentication authentication){
        CustomUserDetails user = SecurityUtils.currentUser(authentication);
        cartService.clear(user.getId());
        return Result.success();

    }
    @PostMapping("/checkout")
    public Result<OrderDetailResponse> checkout(@Valid @RequestBody CartCheckoutRequest request, Authentication authentication){
        //获取当前的登录用户
        CustomUserDetails user =SecurityUtils.currentUser(authentication);
        //调用购物车的业务层，完成结算的功能
        OrderDetailResponse detailResponse= cartService.checkout(user.getId(), request);
        //返回结果
        return Result.success(detailResponse);
    }

}

