package com.example.ecommerce.security;

import com.example.ecommerce.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 自定义用户详情类，实现 Spring Security 的 UserDetails 接口。
 * 将数据库 User 实体转换为 Spring Security 能理解的格式。
 */
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final Integer status;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Long id, String username, String password,
                             Integer status, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.status = status;
        this.authorities = authorities;
    }

    /**
     * 工厂方法：从 User 实体创建 CustomUserDetails。
     * 角色映射：所有用户都有 ROLE_USER，管理员额外有 ROLE_ADMIN。
     */
    public static CustomUserDetails fromUser(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return new CustomUserDetails(
                user.getId(), user.getUsername(), user.getPassword(),
                user.getStatus(), authorities
        );
    }

    public Long getId() { return id; }
    public boolean isAdmin() {
        return authorities.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return status == null || status >= 0; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return status == null || status == 1; }
}