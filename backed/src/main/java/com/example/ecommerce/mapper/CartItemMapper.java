package com.example.ecommerce.mapper;

import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface CartItemMapper {
    CartItem selectById(@Param("id") Long id);

    CartItem selectByUserAndProduct(@Param("userId") Long  userId, @Param("productId") Long productId);

    List<CartItemResponse> selectCartByUserId(@Param("userId") Long userId);

    int insert(CartItem cartItem);

    int updateQuantity(@Param("id") Long id, @Param("userId") Long userId, @Param("quantity") Integer quantity);

    int increaseQuantity(@Param("id") Long id, @Param("userId") Long userId);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int deleteByUserId(@Param("userId") Long userId);

    int deleteCheckedCartItems(@Param("userId") Long userId, @Param("ids") List<Long> ids);






}
