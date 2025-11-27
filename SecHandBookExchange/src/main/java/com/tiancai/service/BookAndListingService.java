package com.tiancai.service;

import com.tiancai.dto.ListingCreateDTO;
import com.tiancai.dto.ListingDetailDTO;
import com.tiancai.entity.PageResult;

public interface BookAndListingService {
    PageResult search(String keyword, Integer categoryId, String sortBy, Integer page, Integer pageSize);

    ListingDetailDTO findById(Integer id);

    Integer create(ListingCreateDTO listingCreateDTO, Integer currentUserId);

    void delete(Integer id, Integer currentUserId);
}
