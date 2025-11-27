package com.tiancai.mapper;

import com.tiancai.entity.Book;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper {
    Book findByIsbn(String isbn);

    void insert(Book book);
    
    // 插入书籍和分类的关联
    void insertBookCategory(Integer bookId, Integer categoryId);
}
