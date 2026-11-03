package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.*;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.security.CustomUserDetails;
import com.example.ecommerce.security.TokenService;
import com.example.ecommerce.service.AuthService;
import com.example.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/*
*
* web层
* 认证控制器--处理登录 注册 退出
* */
@Tag(name="认证接口",description = "用户登录 注册 退出")
@RestController//将该类定义为处理器类（接受请求--浏览器端发送过来到请求和响应--将处理的将结果返回浏览器）
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final TokenService tokenService;
    private final UserService userService;

    public AuthController(AuthService authService, TokenService tokenService, UserService userService) {
        this.authService = authService;
        this.tokenService = tokenService;
        this.userService = userService;
    }
    /*
     * 登录的请求 http://localhost:8080/api/auth/login
     * 前端: LoginVue.vue,const res = await http.post('/auth/login',form.value)  form.value(用户名、密码)
     * @param request  是AuthLoginRequest类型，是登录请求的DTO，包含了用户名和密码
     * @return Result 统一的响应封装类
     * @Valid 支持认证的注解   @RequestBody post请求支持数据协议体的方式传递接收
     */
    @PostMapping("/login")
   public Result<AuthResponse> login(@Valid @RequestBody AuthLoginRequest request){
       AuthResponse res = authService.login(request.getUsername(), request.getPassword());
       return Result.success(res);
  }
    
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest request){
        User user = authService.register(request);
        return Result.success(user);
    }
    @GetMapping("/me")//只接受get请求
    public Result<User> getCurrentUser(@RequestHeader(value = "Authorization",required = false) String authHeader){
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return Result.failure(Result.UNAUTHORIZED_CODE,"未提供有效token认证");
        }
        CustomUserDetails userDetails =  tokenService.parseToken(authHeader.substring(7));
        if(userDetails == null){
            return Result.failure(401,"token无效或已经过期");
        }

        User user=userService.getById(userDetails.getId());
        if(user == null){
            return Result.failure(Result.NOT_FOUND_CODE,"用户不存在");
        }
        return Result.success(user);
    }
    //退出请求
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization",required = false)String authHeader){
        if(authHeader == null || !authHeader.startsWith("Bearer ")){

        }
        return Result.success();
    }
    @PutMapping("/updateNickname")
    public Result<User> updateNickname(@Valid @RequestBody UpdateNicknameRequest request,
                                       @RequestHeader(value = "Authorization",required = false) String authHeader){
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return Result.failure(Result.UNAUTHORIZED_CODE,"未提供有效token认证");
        }
        CustomUserDetails userDetails = tokenService.parseToken(authHeader.substring(7));
        if(userDetails == null){
            return Result.failure(401,"token无效或已经过期");
        }
        Long userId = userDetails.getId();
        User user = userService.updateNickname(userId, request.getNickname());
        return Result.success(user);
    }
    @PutMapping("/updateEmail")
    public Result<User> updateEmail(@Valid @RequestBody UpdateEmailRequest request,
                                    @RequestHeader(value = "Authorization",required = false) String authHeader){
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return Result.failure(Result.UNAUTHORIZED_CODE,"未提供有效token认证");
        }
        CustomUserDetails userDetails = tokenService.parseToken(authHeader.substring(7));
        if(userDetails == null){
            return Result.failure(Result.UNAUTHORIZED_CODE,"token无效或已经过期");
        }
        Long userId = userDetails.getId();
        User user = userService.updateEmail(userId, request.getEmail());
        return Result.success(user);
    }
    @PutMapping("/updatePhone")
    public Result<User> updatePhone(@Valid @RequestBody UpdatePhoneRequest request,
                                    @RequestHeader(value = "Authorization",required = false) String authHeader){
        //校验Bearer Token请求头
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return Result.failure(Result.UNAUTHORIZED_CODE,"未提供有效token认证");
        }
        CustomUserDetails userDetails = tokenService.parseToken(authHeader.substring(7));
        if(userDetails == null){
            return Result.failure(Result.UNAUTHORIZED_CODE,"token无效或已经过期");
        }
        //从token解析获取当前登录用户ID，前端禁止传userId
        Long userId = userDetails.getId();
        User user = userService.updatePhone(userId, request.getPhone());
        return Result.success(user);
    }
}
