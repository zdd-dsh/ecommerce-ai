package com.example.ecommerce.controller;

import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.CartItemRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.security.TokenService;
import com.example.ecommerce.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CartController 切片测试。
 * 使用 @WebMvcTest + MockMvc，不启动数据库；用 @MockBean 替换 Service 与安全依赖。
 */
@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    // TokenAuthenticationFilter 是被 @WebMvcTest 加载的 Filter，其构造器依赖 TokenService
    @MockBean
    private TokenService tokenService;

    private CartItemResponse buildCartItem(Long id, String name, int qty) {
        CartItemResponse item = new CartItemResponse();
        item.setId(id);
        item.setProductId(10L);
        item.setProductName(name);
        item.setPrice(new BigDecimal("99.00"));
        item.setQuantity(qty);
        return item;
    }

    @Test
    void getCart_returnsList() throws Exception {
        when(cartService.getCart(1L))
                .thenReturn(List.of(buildCartItem(1L, "键盘", 2), buildCartItem(2L, "鼠标", 1)));

        mockMvc.perform(get("/api/cart").with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(Result.SUCCESS))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].productName").value("键盘"));
    }

    @Test
    void addItem_success_returns200() throws Exception {
        doNothing().when(cartService).addItem(eq(1L), any(CartItemRequest.class));

        mockMvc.perform(post("/api/cart/items")
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(Result.SUCCESS));
    }

    @Test
    void addItem_insufficientStock_returns409() throws Exception {
        doThrow(new BusinessException(Result.CONFLICT_CODE, "商品库存异常"))
                .when(cartService).addItem(eq(1L), any(CartItemRequest.class));

        mockMvc.perform(post("/api/cart/items")
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":999}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(Result.CONFLICT_CODE));
    }

    @Test
    void addItem_emptyParams_returns400() throws Exception {
        doThrow(new BusinessException(Result.BAD_REQUEST_CODE, "Product and quantity are required"))
                .when(cartService).addItem(eq(1L), any(CartItemRequest.class));

        mockMvc.perform(post("/api/cart/items")
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":null,\"quantity\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(Result.BAD_REQUEST_CODE));
    }

    @Test
    void removeItem_success_returns200() throws Exception {
        doNothing().when(cartService).removeItem(eq(1L), anyLong());

        mockMvc.perform(delete("/api/cart/items/{id}", 5L).with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(Result.SUCCESS));
    }
}
