package com.tiancai.service.impl;

import com.tiancai.dto.ListingCreateDTO;
import com.tiancai.dto.ListingDTO;
import com.tiancai.dto.ListingDetailDTO;
import com.tiancai.entity.Book;
import com.tiancai.entity.Listing;
import com.tiancai.exception.ForbiddenException;
import com.tiancai.exception.ResourceNotFoundException;
import com.tiancai.exception.ResourceNotFoundException;
import com.tiancai.mapper.BookAndListingMapper;
import com.tiancai.entity.PageResult;
import com.tiancai.mapper.BookMapper;
import com.tiancai.service.BookAndListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookAndListingServiceImpl implements BookAndListingService {

    @Autowired
    private BookAndListingMapper bookAndListingMapper;
    @Autowired
    private BookMapper bookMapper;

    @Override
    public PageResult search(String keyword, Integer categoryId, String sortBy, Integer page, Integer pageSize) {
        // 1. 调用Mapper查询总记录数
        Long total = bookAndListingMapper.countListings(keyword, categoryId);

        // 2. 调用Mapper查询当前页的数据列表
        List<ListingDTO> items = bookAndListingMapper.findPaginatedListings(keyword, categoryId, sortBy, page, pageSize);

        // 3. 封装成PageResult对象并返回
        return new PageResult<>(total, page, pageSize, items);
    }

    @Override
    public ListingDetailDTO findById(Integer id) {
        // 直接调用Mapper方法
        return bookAndListingMapper.findDetailById(id);
    }

    @Override
    @Transactional
    public Integer create(ListingCreateDTO dto, Integer sellerId) {
        Book book = bookMapper.findByIsbn(dto.getIsbn());

        if (book == null) {
            book = new Book();
            // ... 从 dto 中设置 book 的属性 ...
            book.setIsbn(dto.getIsbn());
            book.setTitle(dto.getTitle());
            book.setAuthor(dto.getAuthor());
            book.setPublisher(dto.getPublisher());
            book.setPublicationYear(dto.getPublicationYear());
            book.setCoverImageUrl(dto.getCoverImageUrl());
            // ...
            bookMapper.insert(book); // 插入后，book.getBookId() 会被回填
            
            // 插入书籍和分类的关联
            if (dto.getCategoryId() != null) {
                bookMapper.insertBookCategory(book.getBookId(), dto.getCategoryId());
            }
        }

        Listing listing = new Listing();
        listing.setSellerId(sellerId);
        listing.setBookId(book.getBookId());
        listing.setPrice(dto.getPrice());
        listing.setConditionDesc(dto.getConditionDesc());
        listing.setListingType(dto.getListingType());
        listing.setDescription(dto.getDescription());

        bookAndListingMapper.insert(listing); // 插入后，listing.getListingId() 会被回填

        return listing.getListingId();
    }

    @Override
    @Transactional
    public void delete(Integer listingId, Integer currentUserId) {
        // 1. 检查发布信息是否存在
        Listing listing = bookAndListingMapper.findSimpleById(listingId);
        if (listing == null) {
            throw new ResourceNotFoundException("您要操作的发布信息不存在"); // 抛出404异常
        }

        // 2. 权限校验：检查当前用户是否是该信息的发布者
        if (!listing.getSellerId().equals(currentUserId)) {
            throw new ForbiddenException("无权操作不属于自己的发布信息"); // 抛出403异常
        }

        // 3. 执行逻辑删除（更新状态）
        bookAndListingMapper.updateStatus(listingId, "已下架");
    }
}
