package com.example.ecommerce.config;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.CartItemRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.security.CustomUserDetails;
import com.example.ecommerce.security.SecurityUtils;
import com.example.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="购物车接口", description = "购物车的查询和添加等功能")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    //在Web层，要使用Service业务层的数据
    private final CartService cartService;

    //构造方法，完成对cartService的赋值（构造器注入）
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    @GetMapping
    public Result<List<CartItemResponse>> getCart(Authentication authentication){
        //从Authentication对象中提取当前用户信息
        CustomUserDetails user = SecurityUtils.currentUser(authentication);
        //在web层去调用业务层的数据
        List<CartItemResponse> cartList=cartService.getCart(user.getId());
        return Result.success(cartList);
    }

    @PostMapping("/items")
    public Result<Void> addItem(@RequestBody CartItemRequest request, Authentication authentication){
        CustomUserDetails user =SecurityUtils.currentUser(authentication);
        //调用业务层，完成加入购物车的功能
        cartService.addItem(user.getId(), request);
        return Result.success();
    }
    /*
     * 更新购物车商品数量的请求
     * @param id 购物车项id
     * @param request 购物车商品请求对象（包含商品id，要修改的新的商品的数量）
     * @param authentication 通过authentication来获取当前用户
     */
    public Result<Void> updateItem(@PathVariable Long id, @RequestBody CartItemRequest request, Authentication authentication){
        //获取当前的登录用户
        CustomUserDetails user =SecurityUtils.currentUser(authentication);
        //从web层（控制层） 去调用 service业务层
        cartService.updateItem(user.getId(), id, request);
        //返回Result
        return Result.success();
    }


}
