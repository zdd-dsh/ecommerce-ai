package com.example.ecommerce.mapper;

import com.example.ecommerce.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface UserAddressMapper {
    UserAddress selectById(Long id);

    /*
     * 根据用户的id查询地址列表（一个用户下可以有多个地址）
     * @param userId 用户的id
     * @return List集合，存储的是地址列表
     */
    List<UserAddress> selectByUserId(Long userId);

    /*
     * 根据用户的id查询默认地址
     * @param userId 用户id
     * @return UserAddress，表示默认地址，返回值可以为空，说明没有设置默认地址
     */
    UserAddress selectDefaultByUserId(Long userId);

    /*
     * 新增一个用户地址
     * @param userAddress 要新增的地址
     * @return
     */
    int insert(UserAddress userAddress);

    /*
     * 更新用户地址（带userId校验）
     * @param userAddress 要更新的用户地址信息
     * @return
     */
    int update(UserAddress userAddress);

    int delete(@Param("id") Long id, @Param("userId") Long userId);

    int clearDefaultByUserId(Long userId);

}
