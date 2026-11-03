package com.example.book.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.book.common.Result;
import com.example.book.entity.User;
import com.example.book.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("hello springboot");
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        return Result.success(user);
    }

    @PostMapping("/add")
    public Result<String> addUser(@Valid @RequestBody User user) {
        userMapper.insert(user);
        return Result.success("新增成功，id=" + user.getId());
    }

    @GetMapping("/list")
    public Result<List<User>> list() {
        List<User> list = userMapper.selectList(null);
        return Result.success(list);
    }
}
