package com.tiancai.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ListingDetailDTO {
    private Integer listingId;
    private BigDecimal price;
    private String conditionDesc; // 注意：文档里是 condition，但表里是 condition_desc
    private String listingType;
    private String status;
    private LocalDateTime postTime;
    private String description;

    // 嵌套对象
    private SellerDTO seller;
    private BookDetailDTO book;
}
