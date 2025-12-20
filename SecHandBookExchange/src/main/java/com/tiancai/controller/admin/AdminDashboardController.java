package com.tiancai.controller.admin;

import com.tiancai.entity.Result;
import com.tiancai.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理员 - 数据统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private AdminService adminService;

    /**
     * 获取数据统计
     */
    @GetMapping("/statistics")
    public Result getStatistics() {
        Map<String, Object> statistics = adminService.getStatistics();
        return Result.success(statistics);
    }
}


