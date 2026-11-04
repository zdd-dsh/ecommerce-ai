package com.example.ecommerce.controller;

import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.AuthResponse;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.security.TokenService;
import com.example.ecommerce.service.AuthService;
import com.example.ecommerce.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController 切片测试。
 * 使用 @WebMvcTest + MockMvc，不启动数据库；用 @MockBean 替换 Service 与安全依赖。
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Controller 构造器依赖
    @MockBean
    private AuthService authService;
    @MockBean
    private TokenService tokenService;
    @MockBean
    private UserService userService;

    private static final String VALID_REGISTER_BODY =
            "{\"username\":\"alice\",\"password\":\"password123\",\"confirmPassword\":\"password123\","
                    + "\"nickname\":\"Alice\",\"email\":\"alice@example.com\",\"phone\":\"13800138000\"}";

    private User buildUser(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        return u;
    }

    @Test
    void login_success_returnsToken() throws Exception {
        User user = buildUser(1L, "alice");
        when(authService.login("alice", "correct"))
                .thenReturn(AuthResponse.of("fake-token", user));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"correct\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(Result.SUCCESS))
                .andExpect(jsonPath("$.data.token").value("fake-token"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        when(authService.login("alice", "wrong"))
                .thenThrow(new BusinessException(Result.UNAUTHORIZED_CODE, "Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(Result.UNAUTHORIZED_CODE));
    }

    @Test
    void register_usernameExists_returns409() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new BusinessException(Result.CONFLICT_CODE, "该用户名已被注册"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTER_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(Result.CONFLICT_CODE));
    }

    @Test
    void login_emptyPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(Result.BAD_REQUEST_CODE));
    }

    @Test
    void register_success_returns200WithUser() throws Exception {
        User user = buildUser(1L, "alice");
        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTER_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(Result.SUCCESS))
                .andExpect(jsonPath("$.data.username").value("alice"));
    }
}
