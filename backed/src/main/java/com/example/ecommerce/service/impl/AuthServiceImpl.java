package com.example.ecommerce.service.impl;

import com.example.ecommerce.common.BusinessException;
import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.AuthResponse;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.security.CustomUserDetails;
import com.example.ecommerce.security.TokenService;
import com.example.ecommerce.service.AuthService;
import com.example.ecommerce.service.UserService;
import lombok.extern.flogger.Flogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger log= LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TokenService tokenService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserService userService, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Override
    public AuthResponse login(String username, String password) {
        try{
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username,password));
            //CustomUserDetails自定义用户详情类，实现 Spring Security 的 UserDetails 接口。
            CustomUserDetails userDetails= (CustomUserDetails) auth.getPrincipal();
            //获取token
            String token = tokenService.createToken(userDetails);
            //获取用户信息（通过Service业务层来获取）
            User user= userService.getByUsername(username);
            log.info("用户登录成功：{}",username);
            //借用AuthResponse，将token和用户信息user返回
            return AuthResponse.of(token,user);
        }catch (BadCredentialsException ex){
            log.info("用户登录失败：{}",username);
            //Result.UNAUTHORIZED_CODE  401 登录失败的异常状态码
            throw new BusinessException(Result.UNAUTHORIZED_CODE,"Invalid username or password");
        }
    }
    /*
     * 注册认证服务
     * 注册信息不能为空、两次密码要一致、校验用户是否已经注册过了
     * @param request   注册请求的DTO，包含了注册页面全部字段的验证规则
     * @return          User对象
     */
    @Override
    public User register(RegisterRequest request) {
        //注册信息不能为空的情况
        if(request==null){
            //Result.BAD_REQUEST_CODE 400参数错误状态码
            throw new BusinessException(Result.BAD_REQUEST_CODE,"注册信息不能为空");
        }
        //校验两次密码要一致
//        log.info("密码：{}",request.getPassword());
//        log.info("确认密码：{}",request.getConfirmPassword());

        if(request.getPassword() == null || !request.getPassword().equals(request.getConfirmPassword())){
            //Result.CONFLICT_CODE 数据冲突的状态码
            throw new BusinessException(Result.CONFLICT_CODE,"两次输入的密码不一致");
        }
        //校验用户是否已经注册过了

        if(userService.getByUsername(request.getUsername()) != null){
            throw new BusinessException(Result.CONFLICT_CODE,"该用户名已被注册");
        }
        // 创建User对象
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(userService.encodeIfNecessary(request.getPassword()));
        user.setNickname(request.getNickname().trim());
        user.setEmail(request.getEmail().trim());
        user.setPhone(request.getPhone().trim()); //"13811113333"-->"13811113333"
        user.setRole("user"); //"user"  "admin"
        user.setStatus(1);    //0 1

        userService.save(user);
        log.info("新用户注册成功：{}",user.getUsername());
        return userService.getByUsername(user.getUsername());

    }

    @Override
    public void logout(String token) {
        log.info("用户退出登录");
//移除用户的token
        tokenService.revokeToken(token);
    }
}
