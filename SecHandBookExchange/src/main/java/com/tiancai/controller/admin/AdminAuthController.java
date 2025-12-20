package com.tiancai.controller.admin;

import com.tiancai.dto.AdminLoginDTO;
import com.tiancai.entity.Admin;
import com.tiancai.entity.Result;
import com.tiancai.service.AdminService;
import com.tiancai.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    @Autowired
    private AdminService adminService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody AdminLoginDTO adminLoginDTO) {
        log.info("管理员登录: {}", adminLoginDTO.getUsername());
        
        Admin admin = adminService.login(adminLoginDTO);
        
        // 生成JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", admin.getAdminId());
        claims.put("username", admin.getUsername());
        claims.put("role", admin.getRole());
        claims.put("type", "admin");  // 标识这是管理员token
        
        String token = JwtUtils.generateToken(claims);
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("adminId", admin.getAdminId());
        data.put("username", admin.getUsername());
        data.put("role", admin.getRole());
        
        return Result.success(data);
    }
}


