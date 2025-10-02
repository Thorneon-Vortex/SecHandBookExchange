package com.tiancai.controller;

import com.tiancai.entity.Category;
import com.tiancai.entity.Result;
import com.tiancai.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@Slf4j
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * 4.1 获取所有书籍分类
     */
    @GetMapping
    public Result getAllCategories() {
        log.info("查询所有分类");
        
        try {
            List<Category> categories = categoryService.getAllCategories();
            return Result.success(categories);
        } catch (Exception e) {
            log.error("查询分类失败", e);
            return Result.error(e.getMessage());
        }
    }
}

