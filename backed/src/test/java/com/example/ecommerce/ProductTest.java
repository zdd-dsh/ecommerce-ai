package com.example.ecommerce;

// 删掉错误的 import org.apache.commons.math3.stat.descriptive.summary.Product;
import com.example.ecommerce.dto.ProductQueryRequest;
import com.example.ecommerce.entity.Product; //导入自己的实体类
import com.example.ecommerce.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.parameters.P;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
public class ProductTest {
    @Autowired
    private ProductMapper productMapper;
    @Test
    void Test01(){
        // 创建商品类
        Product p = new Product();
        p.setName("平板电脑");
        System.out.println(p);
    }
    @Test
    void test02(){
        ProductQueryRequest pqr = new ProductQueryRequest();
        //pqr.setPage(8);
        //System.out.println("规范后的页码："+pqr.getNormalizedPage());

        pqr.setPageSize(3);
        System.out.println("规范后的每页数量："+pqr.getNormalizedPageSize()); //规范后的每页数量：3

        pqr.setPageSize(10000);
        System.out.println("规范后的每页数量："+pqr.getNormalizedPageSize()); //规范后的每页数量：50
    }
    @Test
    void test03(){
        ProductQueryRequest pqr = new ProductQueryRequest();
        //pqr.setPageSize(8);  //每页显示8条数据
        //pqr.setPage(3);      //第3页
        //System.out.println(pqr.getOffset());

//        pqr.setSortBy(" latest ");
//        System.out.println(pqr.getNormalizedSortBy());
        pqr.setSortDirection("asc");
        System.out.println(pqr.getNormalizedSortDirection());
    }
    @Test
    void test04(){
//        Product p = productMapper.selectById(11L);
//        System.out.println(p);
//        List<Product> products=productMapper.selectAll();
//        System.out.println(products);
//          ProductQueryRequest request= new ProductQueryRequest();
//          request.setKeyword("智");
//          request.setCategoryId(1);
//          System.out.println("keyword:"+request.getKeyword());
//          request.setMinPrice(new BigDecimal(300));
//          request.setMaxPrice(new BigDecimal(600));
//          request.setSortBy("stock");
//          request.setSortDirection("desc");

//          List<Product> products = productMapper.selectPage(request);
//          System.out.println(products);

//        long num = productMapper.countPage(request);
//        System.out.println("数据总条数"+num);
//        List<Product> products = productMapper.selectByCategoryId(1);
//        System.out.println(products);
//        Product p = new Product();
//        p.setName("笔记本电脑");
//        p.setDescription("不错");
//        p.setPrice(new BigDecimal(7000.3));
//        p.setStock(300);
//        System.out.println("插入前"+p);
//        productMapper.insert(p);
//        System.out.println("插入后"+p);
//        Product p = new Product();
//        p.setId(19L);
//        p.setName("台式机电脑");
//        p.setPrice(new BigDecimal(10000));
//        p.setCategoryId(6);
//        productMapper.update(p);
//        System.out.println("修改成功");
//        productMapper.delete(19L);
//        System.out.println("删除成功");
        productMapper.decreaseStock(3L,10);

    }

}