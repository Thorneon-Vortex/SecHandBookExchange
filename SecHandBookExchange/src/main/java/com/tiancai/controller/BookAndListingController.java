package com.tiancai.controller;

import com.tiancai.dto.ListingCreateDTO;
import com.tiancai.dto.ListingDetailDTO;
import com.tiancai.entity.PageResult;
import com.tiancai.entity.Result;
import com.tiancai.exception.ForbiddenException;
import com.tiancai.exception.ResourceNotFoundException;
import com.tiancai.service.BookAndListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/listings")
@RestController
public class BookAndListingController {

    @Autowired
    private BookAndListingService listingService;

    @GetMapping
    // 1. 将返回值从 Result<PageResult> 改为你的 Result
    public Result searchListings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "time_desc") String sortBy,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        // Service层的逻辑完全不变，它返回一个 PageResult 对象
        PageResult pageResult = listingService.search(keyword, categoryId, sortBy, page, pageSize);

        // 2. 调用你的 Result.success(Object) 方法
        // PageResult 对象会被放入 Result 的 data 字段中
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result getListingById(@PathVariable Integer id) {
        ListingDetailDTO listingDetail = listingService.findById(id);

        if (listingDetail == null) {
            // 如果Service层返回null，说明信息不存在
            // 虽然这里也可以直接返回404，但为了统一响应格式，我们返回一个特定的错误信息
            return Result.error("您要查找的发布信息不存在");
        }

        return Result.success(listingDetail);
    }

    @PostMapping
    public Result createListing(@RequestBody ListingCreateDTO listingCreateDTO) {
        // 假设当前登录用户ID为1 (硬编码，实际应从Token中解析)
        Integer currentUserId = 1;

        try {
            Integer newListingId = listingService.create(listingCreateDTO, currentUserId);
            // 根据API文档，data中需要返回新创建的listingId
            return Result.success(Map.of("listingId", newListingId));
        } catch (Exception e) {
            e.printStackTrace(); // 生产环境应使用日志框架
            return Result.error("发布失败，请稍后重试");
        }
    }

    @DeleteMapping("/{id}")
    public Result deleteListing(@PathVariable Integer id) {
        // 假设当前登录用户ID为1 (硬编码)
        Integer currentUserId = 1;
        try {
            listingService.delete(id, currentUserId);
            return Result.success("下架成功"); // 返回成功消息
        } catch (ResourceNotFoundException e) {
            return Result.error(e.getMessage());
        } catch (ForbiddenException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // 生产环境应使用日志框架
            return Result.error("操作失败，请稍后重试");
        }
    }
}
