package com.tiancai.service.impl;

import com.tiancai.entity.Category;
import com.tiancai.mapper.CategoryMapper;
import com.tiancai.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.findAllCategories();
    }
}

