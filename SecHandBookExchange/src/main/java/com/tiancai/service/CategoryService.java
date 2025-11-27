package com.tiancai.service;

import com.tiancai.entity.Category;

import java.util.List;

public interface CategoryService {
    
    /**
     * 获取所有分类
     */
    List<Category> getAllCategories();
}

