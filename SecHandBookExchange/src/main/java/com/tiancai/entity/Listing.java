package com.tiancai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Listing {

    private Integer listingId;
    private Integer sellerId;
    private Integer bookId;
    private BigDecimal price;
    private String conditionDesc;
    private String listingType;
    private String status;
    private LocalDateTime postTime;
    private String description;
}
