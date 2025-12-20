package com.tiancai.controller.admin;

import com.tiancai.entity.PageResult;
import com.tiancai.entity.Result;
import com.tiancai.entity.User;
import com.tiancai.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员 - 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private AdminService adminService;

    /**
     * 获取用户列表（分页）
     */
    @GetMapping
    public Result getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        
        PageResult<User> result = adminService.getUserList(page, pageSize, keyword);
        return Result.success(result);
    }

    /**
     * 更新用户状态（启用/禁用）
     */
    @PutMapping("/{userId}/status")
    public Result updateUserStatus(
            @PathVariable Integer userId,
            @RequestParam Boolean enabled) {
        
        adminService.updateUserStatus(userId, enabled);
        return Result.success("用户状态更新成功");
    }
}


