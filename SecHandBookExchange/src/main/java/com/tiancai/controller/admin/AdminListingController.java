package com.tiancai.controller.admin;

import com.tiancai.entity.PageResult;
import com.tiancai.entity.Result;
import com.tiancai.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员 - 书籍管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/listings")
public class AdminListingController {

    @Autowired
    private AdminService adminService;

    /**
     * 获取书籍列表（分页）
     */
    @GetMapping
    public Result getListingList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        
        PageResult<com.tiancai.dto.AdminListingDTO> result = adminService.getListingList(page, pageSize, keyword, status);
        return Result.success(result);
    }

    /**
     * 下架书籍
     */
    @PutMapping("/{listingId}/take-down")
    public Result takeDownListing(
            @PathVariable Integer listingId,
            @RequestParam(required = false, defaultValue = "管理员下架") String reason) {
        
        adminService.takeDownListing(listingId, reason);
        return Result.success("书籍已下架");
    }
}

