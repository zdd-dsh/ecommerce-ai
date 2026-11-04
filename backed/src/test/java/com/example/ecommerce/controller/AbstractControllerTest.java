package com.example.ecommerce.controller;

import com.example.ecommerce.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Controller 切片测试公共基类。
 * <p>
 * {@code addFilters = false} 时安全过滤器不跑，Controller 方法上的 {@code Authentication}
 * 参数默认为 null；而部分 Controller 会通过 {@code SecurityUtils.currentUser()} 取 userId，
 * null 时直接抛 401。因此需要手动注入 principal 为 {@link CustomUserDetails} 的 Authentication。
 */
public abstract class AbstractControllerTest {

    /**
     * 构造一个带 {@link CustomUserDetails} principal 的认证请求（普通用户 ROLE_USER）。
     * 使用方式：{@code mockMvc.perform(get(...).with(authAs(1L)))}。
     */
    protected RequestPostProcessor authAs(Long userId) {
        return authAs(userId, "ROLE_USER");
    }

    /**
     * 构造一个带 {@link CustomUserDetails} principal 的管理员认证请求（ROLE_ADMIN）。
     */
    protected RequestPostProcessor authAsAdmin(Long userId) {
        return authAs(userId, "ROLE_ADMIN");
    }

    private RequestPostProcessor authAs(Long userId, String role) {
        CustomUserDetails user = new CustomUserDetails(userId, "alice", "pwd", 1,
                AuthorityUtils.createAuthorityList(role));
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(auth);
            request.setUserPrincipal(auth);
            return request;
        };
    }
}
