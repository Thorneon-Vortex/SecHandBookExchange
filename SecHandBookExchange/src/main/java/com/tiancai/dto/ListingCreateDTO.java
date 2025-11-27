package com.tiancai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ListingCreateDTO {
    // Book 表相关信息
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String publicationYear;
    private String coverImageUrl;
    private Integer categoryId; // 书籍分类ID

    // Listing 表相关信息
    // 注意：实际项目中，sellerId应该从当前登录用户的Token中获取，而不是由前端传递。
    // 这里为了简化，我们暂时假设它被包含在DTO中。
    // private Integer sellerId;

    private BigDecimal price;
    @JsonProperty("condition") // <--- 添加这个注解
    private String conditionDesc; // 对应API文档中的 condition
    private String listingType;
    private String description;
}
