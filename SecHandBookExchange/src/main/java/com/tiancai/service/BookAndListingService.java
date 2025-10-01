package com.tiancai.service;

import com.tiancai.pojo.PageResult;

public interface BookAndListingService {
    PageResult search(String keyword, Integer categoryId, String sortBy, Integer page, Integer pageSize);
}
