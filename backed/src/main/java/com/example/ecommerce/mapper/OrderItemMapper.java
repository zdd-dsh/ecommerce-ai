package com.example.ecommerce.mapper;

import com.example.ecommerce.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface OrderItemMapper {
    /*
     * 查询某订单的全部明细
     * @param orderId 订单的主键id
     * @return 集合，集合中存储的OrderItem，订单中的商品明细
     */
    List<OrderItem> selectByOrderId(Long orderId);

    /*
     * 插入商品明细
     * @param orderItem
     * @return
     */
    int insert(OrderItem orderItem);

    /*
     * 批量插入
     * @param items 集合类型，存储多个商品明细
     * @return
     */
    int batchInsert(@Param("items") List<OrderItem> items);

    /*
     * 根据订单id删除对应的商品明细
     * @param orderId 订单的主键id
     * @return
     */
    int deleteByOrderId(Long orderId);

}
