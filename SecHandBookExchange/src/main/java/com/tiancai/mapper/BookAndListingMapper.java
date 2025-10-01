package com.tiancai.mapper;

import com.tiancai.pojo.Listing;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BookAndListingMapper {
    //搜索/浏览书籍发布信息
    List<Listing> searchBook(String bookName);
}
