package com.tiancai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ListingDetailDTO {
    private Integer listingId;
    private BigDecimal price;
    
    @JsonProperty("condition")  // 前端使用 condition，后端映射到 conditionDesc
    private String conditionDesc;
    
    private String listingType;
    private String status;
    private LocalDateTime postTime;
    private String description;

    // 嵌套对象
    private SellerDTO seller;
    private BookDetailDTO book;
}
