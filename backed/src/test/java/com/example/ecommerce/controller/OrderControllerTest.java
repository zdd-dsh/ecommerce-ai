package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.OrderDetailResponse;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.security.TokenService;
import com.example.ecommerce.service.OrderService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OrderController 切片测试。
 * 使用 @WebMvcTest + MockMvc，不启动数据库；用 @MockBean 替换 Service 与安全依赖。
 */
@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    // TokenAuthenticationFilter 是被 @WebMvcTest 加载的 Filter，其构造器依赖 TokenService
    @MockBean
    private TokenService tokenService;

    private Order buildOrder(Long id, Long userId, Integer status) {
        Order order = new Order();
        order.setId(id);
        order.setUserId(userId);
        order.setOrderNo("NO" + id);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("99.00"));
        order.setReceiver("张三");
        order.setPhone("13800138000");
        order.setAddress("北京市朝阳区某某街道");
        return order;
    }

    private OrderDetailResponse buildDetail(Order order) {
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setOrderId(order.getId());
        item.setProductId(10L);
        item.setProductName("键盘");
        item.setPrice(new BigDecimal("99.00"));
        item.setQuantity(1);
        return new OrderDetailResponse(order, List.of(item));
    }

    // ===== getAll 角色分支 =====

    @Test
    void getAll_regularUser_callsGetByUserId() throws Exception {
        when(orderService.getByUserId(1L)).thenReturn(List.of(buildOrder(1L, 1L, 1)));

        mockMvc.perform(get("/api/orders").with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.SUCCESS));

        verify(orderService).getByUserId(1L);
        verify(orderService, never()).getAll();
    }

    @Test
    void getAll_adminUser_callsGetAll() throws Exception {
        when(orderService.getAll()).thenReturn(List.of(buildOrder(1L, 1L, 1)));

        mockMvc.perform(get("/api/orders").with(authAsAdmin(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.SUCCESS));

        verify(orderService).getAll();
        verify(orderService, never()).getByUserId(any());
    }

    // ===== getById =====

    @Test
    void getById_success() throws Exception {
        when(orderService.getById(1L)).thenReturn(buildOrder(1L, 1L, 1));

        mockMvc.perform(get("/api/orders/{id}", 1L).with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.SUCCESS))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(orderService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/orders/{id}", 999L).with(authAs(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(Result.NOT_FOUND_CODE));
    }

    @Test
    void getById_forbidden_returns403() throws Exception {
        // 订单归属用户 2，请求方是用户 1（非管理员）
        when(orderService.getById(1L)).thenReturn(buildOrder(1L, 2L, 1));

        mockMvc.perform(get("/api/orders/{id}", 1L).with(authAs(1L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(Result.FORBIDDEN_CODE));
    }

    // ===== getDetail =====

    @Test
    void getDetail_success_returns200() throws Exception {
        Order order = buildOrder(1L, 1L, 1);
        when(orderService.getDetailById(1L)).thenReturn(buildDetail(order));

        mockMvc.perform(get("/api/orders/{id}/detail", 1L).with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.SUCCESS))
                .andExpect(jsonPath("$.data.order.id").value(1));
    }

    // ===== getByOrderId =====

    @Test
    void getByOrderId_success() throws Exception {
        when(orderService.getByOrderNo("NO1")).thenReturn(buildOrder(1L, 1L, 1));

        mockMvc.perform(get("/api/orders/orderNo/{orderNo}", "NO1").with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.SUCCESS))
                .andExpect(jsonPath("$.data.orderNo").value("NO1"));
    }

    // ===== getByUserId =====

    @Test
    void getByUserId_otherUser_returns403() throws Exception {
        // 用户 1 查询用户 2 的订单，非管理员 → 403
        mockMvc.perform(get("/api/orders/user/{userId}", 2L).with(authAs(1L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(Result.FORBIDDEN_CODE));
    }

    // ===== save =====

    @Test
    void save_success_returns200() throws Exception {
        String body = "{\"order\":{\"receiver\":\"张三\",\"phone\":\"13800138000\",\"address\":\"北京市朝阳区\"},"
                + "\"orderItems\":[{\"productId\":1,\"quantity\":2}]}";
        when(orderService.save(any(Order.class), any())).thenReturn(buildDetail(buildOrder(1L, 1L, 0)));

        mockMvc.perform(post("/api/orders")
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.SUCCESS))
                .andExpect(jsonPath("$.data.order.id").value(1));
    }

    @Test
    void save_nullOrder_returns400() throws Exception {
        String body = "{\"order\":null,\"orderItems\":[]}";

        mockMvc.perform(post("/api/orders")
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(Result.BAD_REQUEST_CODE));
    }

    // ===== update 角色分支 =====

    @Test
    void update_regularUserConfirmReceipt_success() throws Exception {
        when(orderService.getById(1L)).thenReturn(buildOrder(1L, 1L, 1)); // 已支付
        doNothing().when(orderService).update(any(Order.class));

        String body = "{\"id\":1,\"targetStatus\":2}";
        mockMvc.perform(put("/api/orders")
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.SUCCESS));

        verify(orderService).update(any(Order.class));
    }

    @Test
    void update_regularUserNotPaid_returns409() throws Exception {
        when(orderService.getById(1L)).thenReturn(buildOrder(1L, 1L, 0)); // 待支付

        String body = "{\"id\":1,\"targetStatus\":2}";
        mockMvc.perform(put("/api/orders")
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(Result.CONFLICT_CODE));

        verify(orderService, never()).update(any(Order.class));
    }

    @Test
    void update_admin_success() throws Exception {
        when(orderService.getById(1L)).thenReturn(buildOrder(1L, 1L, 1));
        doNothing().when(orderService).update(any(Order.class));

        String body = "{\"id\":1,\"status\":3,\"address\":\"新地址\"}";
        mockMvc.perform(put("/api/orders")
                        .with(authAsAdmin(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.SUCCESS));

        verify(orderService).update(any(Order.class));
    }

    // ===== delete =====

    @Test
    void delete_success_returns200() throws Exception {
        when(orderService.getById(1L)).thenReturn(buildOrder(1L, 1L, 1));
        doNothing().when(orderService).delete(anyLong());

        mockMvc.perform(delete("/api/orders/{id}", 1L).with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.SUCCESS));
    }
}
