package com.example.ecommerce.controller;

import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.common.Result;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.security.TokenService;
import com.example.ecommerce.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ProductController 切片测试。
 * 使用 @WebMvcTest + MockMvc，不启动数据库；用 @MockBean 替换 Service 与安全依赖。
 */
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Controller 的业务依赖
    @MockBean
    private ProductService productService;

    // TokenAuthenticationFilter 是被 @WebMvcTest 加载的 Filter，其构造器依赖 TokenService，
    // 这里 mock 掉，避免加载真实 TokenService（会访问 Redis 并要求 HMAC 密钥配置）。
    @MockBean
    private TokenService tokenService;

    private Product buildProduct(Long id, String name) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setDescription("测试商品");
        p.setPrice(new BigDecimal("99.00"));
        p.setStock(10);
        p.setStatus(1);
        return p;
    }

    @Test
    void getAll_returns200AndResultStructure() throws Exception {
        Product p1 = buildProduct(1L, "键盘");
        Product p2 = buildProduct(2L, "鼠标");
        when(productService.getAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(Result.SUCCESS))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[1].name").value("鼠标"));
    }

    @Test
    void getById_whenExists_returns200() throws Exception {
        Product p = buildProduct(1L, "机械键盘");
        when(productService.getById(1L)).thenReturn(p);

        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(Result.SUCCESS))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("机械键盘"));
    }

    @Test
    void getById_whenNotExists_returnsNon200Code() throws Exception {
        when(productService.getById(999L))
                .thenThrow(new BusinessException(Result.NOT_FOUND_CODE, "商品不存在"));

        mockMvc.perform(get("/api/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(Result.NOT_FOUND_CODE));
    }
}
