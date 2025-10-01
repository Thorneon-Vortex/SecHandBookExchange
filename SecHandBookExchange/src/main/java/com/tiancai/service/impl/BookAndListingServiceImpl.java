package com.tiancai.service.impl;

import com.tiancai.mapper.BookAndListingMapper;
import com.tiancai.pojo.Listing;
import com.tiancai.pojo.PageResult;
import com.tiancai.service.BookAndListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookAndListingServiceImpl implements BookAndListingService {

    @Autowired
    private BookAndListingMapper bookAndListingMapper;

    @Override
    public PageResult search(String keyword, Integer categoryId, String sortBy, Integer page, Integer pageSize) {
        // 1. 调用Mapper查询总记录数
        Long total = bookAndListingMapper.countListings(keyword, categoryId);

        // 2. 调用Mapper查询当前页的数据列表
        List<Listing> items = bookAndListingMapper.findPaginatedListings(keyword, categoryId, sortBy, page, pageSize);

        // 3. 封装成PageResult对象并返回
        return new PageResult<>(total, page, pageSize, items);
    }
}
