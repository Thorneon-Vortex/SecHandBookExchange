package com.tiancai.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理员书籍列表DTO（包含书名信息）
 */
@Data
public class AdminListingDTO {
    private Integer listingId;
    private Integer sellerId;
    private Integer bookId;
    private String bookTitle;      // 书名
    private String author;         // 作者
    private String isbn;           // ISBN
    private BigDecimal price;
    private String conditionDesc;
    private String listingType;
    private String status;
    private LocalDateTime postTime;
    private String description;
}


