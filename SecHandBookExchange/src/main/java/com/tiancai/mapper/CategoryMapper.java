package com.tiancai.mapper;

import com.tiancai.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    
    /**
     * 获取所有分类
     */
    List<Category> findAllCategories();
}

