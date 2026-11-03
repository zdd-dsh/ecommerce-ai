package com.example.ecommerce.service;

import com.example.ecommerce.common.PagedResponse;
import com.example.ecommerce.dto.ProductQueryRequest;
import com.example.ecommerce.entity.Product;
import java.util.List;

public interface ProductService {
    void warmUpCache();
    Product getById(Long id);
    List<Product> getAll();
    PagedResponse<Product> query(ProductQueryRequest requests);
    void save(Product product);
    /*
     * 批量导入商品
     */
    int importProducts(List<Product> products);
    /*
     * 更新商品（管理员）
     */
    void update(Product product);
    /*
     * 删除商品（管理员）
     */
    void delete(Long id);

}