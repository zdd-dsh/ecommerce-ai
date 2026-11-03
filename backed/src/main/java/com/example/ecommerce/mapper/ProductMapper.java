package com.example.ecommerce.mapper;

import com.example.ecommerce.dto.ProductQueryRequest;
import com.example.ecommerce.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface ProductMapper {
    Product selectById(Long id);

    List<Product> selectAll();

    List<Product>  selectPage(@Param("request")ProductQueryRequest request);

    long countPage(@Param("request")ProductQueryRequest request);

    List<Product> selectByCategoryId(Integer categoryId);

    int insert(Product product);

    int update(Product product);

    int delete(Long id);

    int decreaseStock(Long productId,Integer quantity);

}
