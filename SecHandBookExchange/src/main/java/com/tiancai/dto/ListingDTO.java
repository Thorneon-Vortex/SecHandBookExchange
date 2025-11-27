package com.tiancai.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ListingDTO {
    private Integer listingId;
    private String title;
    private String author;
    private BigDecimal price;
    private String listingType;
    private String status;
    private String coverImageUrl;
    private String sellerNickname;
}