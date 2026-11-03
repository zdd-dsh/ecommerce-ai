package com.example.ecommerce.mapper;

import com.example.ecommerce.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    Order selectById(Long id);

    Order selectByOrderNo(String orderNo);

    List<Order> selectByUserId(Long userId);

    List<Order> selectAll();

    int insert(Order order);

    int update(Order ordder);

    int delete(Long order);

    int updateStatusByOrderNo(@Param("orderNo") String orderNo, @Param("status") Integer status);

}
