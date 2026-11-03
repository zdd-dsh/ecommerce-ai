package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddressRequest;
import com.example.ecommerce.entity.UserAddress;

import java.util.List;

public interface UserAddressService {
    /**
     * 根据用户id查询所有的地址（当前用户下会有多个地址）
     * @param userId 用户id
     * @return List集合，存储多个地址
     */
    List<UserAddress> getByUserId(Long userId);

    /**
     * 根据主键id查询对应的地址（一个地址）
     * @param id 主键id
     * @return 地址
     */
    UserAddress getById(Long id);

    /**
     * 新增用户地址
     * @param userId 用户id
     * @param request AddressRequest类，收货地址请求的DTO类（收货人、电话、省、市等等）
     * @return UserAddress类型，表示新增的地址
     */
    UserAddress create(Long userId, AddressRequest request);

    UserAddress update(Long userId,Long id,  AddressRequest request);

    /**
     * 删除用户地址
     * @param userId 用户id
     * @param id 主键id
     */
    void delete(Long userId, Long id);

    /**
     * 设置默认地址
     * @param userId 用户id
     * @param id 主键id
     */
    void setDefault(Long userId, Long id);

    UserAddress getOwnedAddress(Long userId, Long id);

    UserAddress getDefaultAddress(Long userId);


}
