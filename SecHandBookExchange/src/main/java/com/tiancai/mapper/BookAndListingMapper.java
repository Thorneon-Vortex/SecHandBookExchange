package com.tiancai.mapper;

import com.tiancai.pojo.Listing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookAndListingMapper {
    //搜索/浏览书籍发布信息
    //List<Listing> searchBook(String bookName);
    /**
     * 根据条件统计总记录数
     */
    Long countListings(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId
    );

    /**
     * 根据条件进行分页和排序查询
     */
    List<Listing> findPaginatedListings(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("sortBy") String sortBy,
            @Param("page") Integer page,
            @Param("pageSize") Integer pageSize
    );
}
