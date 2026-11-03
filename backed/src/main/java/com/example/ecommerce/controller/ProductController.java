package com.example.ecommerce.controller;

import com.example.ecommerce.common.PagedResponse;
import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.ProductQueryRequest;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * Web层
 * 商品管理控制器类 - 处理商品的查询、管理、导入等功能
 * 浏览器端发送过来的请求有两种：
 * (1)get请求:  浏览器地址栏输入地址回车  超链接发送的请求  http.get(xxxx)
 * (2)post请求  await http.post('/auth/login',form.value)
 * 包含如下设计:
 *  -GET请求    /api/products        -- 获取所有的商品
 *  -GET请求    /api/products/{id}   -- 根据id获取对应商品的详情
 *  -GET请求    /api/products/query  -- 分页的条件查询
 *  -POST请求   /api/products        -- 创建商品(管理员)
 *  -POST请求   /api/products/import -- 批量导入商品(管理员)
 *  -PUT请求    /api/products        -- 更新商品(管理员)
 *  -DELETE请求 /api/products/{id}   -- 删除商品(管理员)
 */
@Tag(name="商品接口", description = "商品查询与管理")
@RestController    //控制器类，该类才具有接收请求和响应的功能
@RequestMapping("/api/products")  //请求路径
public class ProductController {
    /*
     * 文件上传大小限制 - 5MB（防止大文件攻击）
     */
    private static final long MAX_UPLOAD_SIZE = 5 * 1024 * 1024;

    //引入商品的业务层
    private final ProductService productService;
    //构造器完成对productService注入
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @GetMapping
    public Result<List<Product>> getAll(){
        //由web层，去调用业务层
        List<Product> products= productService.getAll();
        System.out.println("获取所有商品："+products);
        //以Result格式，将成功的数据返回到浏览器端
        return Result.success(products);
    }
    /*
     * 根据id获取对应商品的详情
     */
    @GetMapping("/{id}")
//@PathVariable的作用: {id}---> id
//Authentication authentication 是Spring Security和核心接口，它代表当前请求的登录认证对象
    public Result<Product> getById(@PathVariable Long id, Authentication authentication){
        //由web层，去调用业务层，让业务层完成根据id查询单个商品的查询功能
        Product product=productService.getById(id);
        //以Result格式，将单个商品返回到浏览器端
        return Result.success(product);
    }
    @GetMapping("/query")
    public Result<PagedResponse<Product>> query(ProductQueryRequest request){
        //由web层去调用业务层，让业务层完成分页查询的操作
        PagedResponse res= productService.query(request);
        //以Result格式返回
        return Result.success(res);
    }


}

