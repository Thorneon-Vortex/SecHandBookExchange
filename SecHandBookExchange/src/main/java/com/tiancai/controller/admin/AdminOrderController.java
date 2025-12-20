package com.tiancai.controller.admin;

import com.tiancai.entity.PageResult;
import com.tiancai.entity.Result;
import com.tiancai.entity.Order;
import com.tiancai.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员 - 订单管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private AdminService adminService;

    /**
     * 获取订单列表（分页）
     */
    @GetMapping
    public Result getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        
        PageResult<Order> result = adminService.getOrderList(page, pageSize, status);
        return Result.success(result);
    }
}


