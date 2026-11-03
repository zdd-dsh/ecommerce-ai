package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.AddressRequest;
import com.example.ecommerce.entity.UserAddress;
import com.example.ecommerce.security.CustomUserDetails;
import com.example.ecommerce.security.SecurityUtils;
import com.example.ecommerce.service.UserAddressService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="收货地址接口", description = "收货地址的增删改查")
@RestController
@RequestMapping("/api/addresses")
public class UserAddressController {

    //引入用户地址的业务层
    private final UserAddressService userAddressService;

    //构造方法
    public UserAddressController(UserAddressService userAddressService) {
        this.userAddressService = userAddressService;
    }

    /*
     * 查询用户地址列表
     * URL:  /api/addresses
     */
    @GetMapping
    public Result<List<UserAddress>> getAddresses(Authentication authentication){
        //获取登录的用户
        CustomUserDetails user= SecurityUtils.currentUser(authentication);
        //调用业务层，获取数据
        List<UserAddress> addressList= userAddressService.getByUserId(user.getId());
        //返回结果
        return Result.success(addressList);
    }

    /*
     * 新增用户地址的请求
     * @param request AddressRequest类型（包含：收件人、电话、省、市、区、详细地址、默认地址）
     * URL：/api/addresses   接收Post请求
     */
    @PostMapping
    public Result<UserAddress> create(@Valid @RequestBody AddressRequest request, Authentication authentication){
        //获取登录的用户
        CustomUserDetails user= SecurityUtils.currentUser(authentication);
        //调用业务层，完成地址的添加
        UserAddress userAddress=userAddressService.create(user.getId(), request);
        //返回结果
        return Result.success(userAddress);
    }
    /**
     * 修改用户地址的请求
     * URL: /api/addresses/{id}    /api/addresses/1   /api/addresses/2
     */
    @PutMapping("/{id}")
    public Result<UserAddress> update(@PathVariable Long id, @Valid @RequestBody AddressRequest request, Authentication authentication){
        //获取登录的用户
        CustomUserDetails user= SecurityUtils.currentUser(authentication);
        //调用业务层，让业务层完成地址的修改
        UserAddress userAddress = userAddressService.update(user.getId(), id, request);
        //返回结果
        return Result.success(userAddress);
    }
    /**
     * 删除用户地址的请求
     * URL: /api/addresses/{id}   接收delete请求
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication authentication){
        //获取登录的用户
        CustomUserDetails user= SecurityUtils.currentUser(authentication);
        //调用业务层，完成地址的删除
        userAddressService.delete(user.getId(), id);
        //返回结果
        return Result.success();
    }
    /**
     * 设置默认地址的请求
     * URL: /api/addresses/{id}/default
     */
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id, Authentication authentication){
        //获取登录的用户
        CustomUserDetails user= SecurityUtils.currentUser(authentication);
        //调用业务层，完成默认地址的设置
        userAddressService.setDefault(user.getId(), id);
        //返回结果
        return Result.success();
    }


}
