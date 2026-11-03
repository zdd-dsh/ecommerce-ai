package com.example.ecommerce;

import com.example.ecommerce.common.PagedResponse;
import com.example.ecommerce.dto.ProductQueryRequest;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ProductServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
     @Autowired
    private ProductService productService;
    @Test
   void test01() throws JsonProcessingException {
//        Map<Integer,String> labels= ProductServiceImpl.CATEGORY_LABELS;
//        System.out.println(labels);
        /*Product p = new Product();
        p.setId(3L);
        p.setName("鼠标");
        p.setDescription("不错");
        p.setPrice(new BigDecimal(2000.4));
        System.out.println(p);
        String str= objectMapper.writeValueAsString(p);
        System.out.println(str);


        Product p1 = new Product();
        p.setId(4L);
        p.setName("电脑");
        p.setDescription("不错");
        p.setPrice(new BigDecimal(6000.4));
        List<Product> ps = new ArrayList<>();
        ps.add(p);
        ps.add(p1);
        System.out.println(ps);

        String strJson =  objectMapper.writeValueAsString(ps);;
        System.out.println(strJson);

         */
//          List<Product> products=productService.getAll();
//          System.out.println(products);
        Product product = productService.getById(5L);
        System.out.println(product);
    }
    @Test
    void test02()  {
        productService.query(null);

    }
    @Test
    void test03()  {
        //场景A：无任何筛选条件
        ProductQueryRequest request = new ProductQueryRequest();
//不set任何request中的参数，全部使用默认值
        PagedResponse result= productService.query(request);
        System.out.println(result.getItems());    //分页查询的商品
        System.out.println(result.getTotal());    //总页数
        System.out.println(result.getPage());     //当前页
        System.out.println(result.getSize());     //每页的数据

    }
    @Test
    void test04(){
        //场景B：只测试价格区间，设置当前页（无搜索关键词、无分类）
        ProductQueryRequest request = new ProductQueryRequest();
        request.setMinPrice(new BigDecimal("300"));
        request.setMaxPrice(new BigDecimal("3000"));
        request.setSortBy("stock");      //按照库存排序
        request.setSortDirection("desc");//设置排序的方向
        request.setPage(2);              //设置当前页为第2页
        PagedResponse result = productService.query(request);
        System.out.println(result.getItems());
        System.out.println(result.getTotal());
        System.out.println(result.getPage());
        System.out.println(result.getSize());
    }
    @Test

    void test05() {
        //测试新增商品（管理员）的方法
        Product p = new Product();
        p.setName(" 笔记本电脑 ");
        p.setDescription("    笔记本电脑不错");
        p.setPrice(new BigDecimal(3004.3531));
        p.setStock(100);
        p.setImage("        /img/xx.jpg");
        p.setCategoryId(4);   //1-6
        p.setMerchantId(9);
        p.setStatus(0);    //0或者1
        productService.save(p);
        System.out.println("保存成功");

    }
@Test
    void test06() {
        List<Product> products = productService.getAll();

        Product p =productService.getById(19L);
}
    @Test
    void test07() {
            //1.实例化集合
            List<Product> products = new ArrayList<>();

            //p1补全定义
            Product p1 = new Product();
            p1.setName(" 笔记本电脑1 ");
            p1.setDescription("    笔记本电脑不错");
            p1.setPrice(new BigDecimal("3004.3531"));
            p1.setStock(100);
            p1.setImage("        /img/xx.jpg");
            p1.setCategoryId(4);
            p1.setMerchantId(9);
            p1.setStatus(0);

            Product p2 = new Product();
            p2.setName(" 笔记本电脑2 ");
            p2.setDescription("    笔记本电脑不错");
            p2.setPrice(new BigDecimal("3004.3531"));
            p2.setStock(100);
            p2.setImage("        /img/xx.jpg");
            p2.setCategoryId(4);
            p2.setMerchantId(9);
            p2.setStatus(0);

            Product p3 = new Product();
            p3.setName(" 笔记本电脑3 ");
            p3.setDescription("    笔记本电脑不错");
            p3.setPrice(new BigDecimal("3004.3531"));
            p3.setStock(100);
            p3.setImage("        /img/xx.jpg");
            p3.setCategoryId(4);
            p3.setMerchantId(9);
            p3.setStatus(0);

            products.add(p1);
            products.add(p2);
            products.add(p3);

            int num = productService.importProducts(products);
            System.out.println("共批量导入商品数量为："+num);
    }

    @Test
    void test08() {
        Product product = productService.getById(18L);
        System.out.println(product);

        product.setName("平板电脑");
        product.setDescription("平板电脑轻巧方便");
        product.setPrice(new BigDecimal(2000.34655));
        product.setStock(300);

//完成修改操作
        productService.update(product);

//重新根据商品id查询
        Product updateProduct=productService.getById(18L);
        System.out.println(updateProduct);

    }
    //查询商品（将数据库中的数据会在redis中存储）
    @Test
    void test09(){
        productService.getAll();
        productService.getById(20L);
        productService.getById(21L);
        productService.getById(22L);
    }
    @Test
    void test10(){
        productService.delete(21L);
        System.out.println("删除成功");
    }

}
