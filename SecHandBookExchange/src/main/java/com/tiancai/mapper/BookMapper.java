package com.tiancai.mapper;

import com.tiancai.entity.Book;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper {
    Book findByIsbn(String isbn);

    public void insert(Book book);
}
