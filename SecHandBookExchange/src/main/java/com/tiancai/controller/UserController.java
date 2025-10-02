package com.tiancai.controller;

import com.tiancai.dto.UserLoginDTO;
import com.tiancai.dto.UserRegisterDTO;
import com.tiancai.dto.UserUpdateDTO;
import com.tiancai.entity.Result;
import com.tiancai.entity.User;
import com.tiancai.service.UserService;
import com.tiancai.utils.BaseContext;
import com.tiancai.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册: {}", userRegisterDTO);
        User user = userService.register(userRegisterDTO);
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("studentId", user.getStudentId());

        return Result.success(data);
    }

    @PostMapping("/login")
    public Result login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录: {}", userLoginDTO.getStudentId());
        User user = userService.login(userLoginDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        String token = JwtUtils.generateToken(claims);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getUserId());
        data.put("nickname", user.getNickname());

        return Result.success(data);
    }

    @GetMapping("/me")
    public Result getCurrentUser() {
        Integer currentUserId = BaseContext.getCurrentId();
        log.info("获取当前用户信息, ID: {}", currentUserId);
        
        User user = userService.getUserInfoById(currentUserId);
        return Result.success(user);
    }

    @PutMapping("/me")
    public Result updateCurrentUser(@RequestBody UserUpdateDTO userUpdateDTO) {
        Integer currentUserId = BaseContext.getCurrentId();
        log.info("更新用户信息, ID: {}, 数据: {}", currentUserId, userUpdateDTO);
        
        User user = new User();
        BeanUtils.copyProperties(userUpdateDTO, user);
        user.setUserId(currentUserId);
        
        userService.updateUserInfo(user);
        
        User updatedUser = userService.getUserInfoById(currentUserId);
        return Result.success(updatedUser);
    }
}