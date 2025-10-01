package com.tiancai.controller;

import com.tiancai.pojo.PageResult;
import com.tiancai.pojo.Result;
import com.tiancai.service.BookAndListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookAndListingController {

    @Autowired
    private BookAndListingService listingService;

    @GetMapping("/listings")
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
}
