package com.tiancai.mapper;

import com.tiancai.dto.ListingDTO;
import com.tiancai.dto.ListingDetailDTO;
import com.tiancai.entity.Listing;
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
    List<ListingDTO> findPaginatedListings(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("sortBy") String sortBy,
            @Param("page") Integer page,
            @Param("pageSize") Integer pageSize
    );

    ListingDetailDTO findDetailById(Integer id);

    void insert(Listing listing);

    // 根据ID查询一个简单的Listing对象，主要用于获取seller_id
    Listing findSimpleById(Integer id);
    
    // 根据ID查询完整的Listing对象（用于订单模块）
    Listing findById(Integer id);

    // 更新状态
    void updateStatus(@Param("id") Integer id, @Param("status") String status);
}
