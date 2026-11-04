package com.example.ecommerce.controller;

import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.AddressRequest;
import com.example.ecommerce.entity.UserAddress;
import com.example.ecommerce.security.TokenService;
import com.example.ecommerce.service.UserAddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserAddressController 切片测试。
 * 使用 @WebMvcTest + MockMvc，不启动数据库；用 @MockBean 替换 Service 与安全依赖。
 */
@WebMvcTest(UserAddressController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserAddressControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAddressService userAddressService;

    // TokenAuthenticationFilter 是被 @WebMvcTest 加载的 Filter，其构造器依赖 TokenService
    @MockBean
    private TokenService tokenService;

    private static final String VALID_ADDRESS_BODY =
            "{\"receiver\":\"张三\",\"phone\":\"13800138000\",\"province\":\"北京市\",\"city\":\"北京市\","
                    + "\"district\":\"朝阳区\",\"detailAddress\":\"某某街道123号\",\"isDefault\":true}";

    private UserAddress buildAddress(Long id, String receiver) {
        UserAddress addr = new UserAddress();
        addr.setId(id);
        addr.setUserId(1L);
        addr.setReceiver(receiver);
        addr.setPhone("13800138000");
        addr.setProvince("北京市");
        addr.setCity("北京市");
        addr.setDistrict("朝阳区");
        addr.setDetailAddress("某某街道123号");
        addr.setIsDefault(0);
        return addr;
    }

    @Test
    void getAddresses_returnsList() throws Exception {
        when(userAddressService.getByUserId(1L))
                .thenReturn(List.of(buildAddress(1L, "张三"), buildAddress(2L, "李四")));

        mockMvc.perform(get("/api/addresses").with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(Result.SUCCESS))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].receiver").value("张三"));
    }

    @Test
    void create_success_returns200() throws Exception {
        UserAddress created = buildAddress(1L, "张三");
        when(userAddressService.create(eq(1L), any(AddressRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/addresses")
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ADDRESS_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(Result.SUCCESS))
                .andExpect(jsonPath("$.data.receiver").value("张三"));
    }

    @Test
    void create_invalidPhone_returns400() throws Exception {
        String invalidBody =
                "{\"receiver\":\"张三\",\"phone\":\"123\",\"province\":\"北京市\",\"city\":\"北京市\","
                        + "\"district\":\"朝阳区\",\"detailAddress\":\"某某街道123号\"}";

        mockMvc.perform(post("/api/addresses")
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(Result.BAD_REQUEST_CODE));
    }

    @Test
    void update_success_returns200() throws Exception {
        UserAddress updated = buildAddress(1L, "张三改");
        when(userAddressService.update(eq(1L), eq(1L), any(AddressRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/addresses/{id}", 1L)
                        .with(authAs(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ADDRESS_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(Result.SUCCESS))
                .andExpect(jsonPath("$.data.receiver").value("张三改"));
    }

    @Test
    void delete_success_returns200() throws Exception {
        doNothing().when(userAddressService).delete(eq(1L), anyLong());

        mockMvc.perform(delete("/api/addresses/{id}", 5L).with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(Result.SUCCESS));
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new BusinessException(Result.NOT_FOUND_CODE, "收货地址不存在"))
                .when(userAddressService).delete(eq(1L), anyLong());

        mockMvc.perform(delete("/api/addresses/{id}", 999L).with(authAs(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(Result.NOT_FOUND_CODE));
    }

    @Test
    void setDefault_success_returns200() throws Exception {
        doNothing().when(userAddressService).setDefault(eq(1L), anyLong());

        mockMvc.perform(put("/api/addresses/{id}/default", 1L).with(authAs(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(Result.SUCCESS));
    }
}
