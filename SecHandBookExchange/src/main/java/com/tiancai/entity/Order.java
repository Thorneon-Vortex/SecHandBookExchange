package com.tiancai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Integer orderId;
    private Integer listingId;
    private Integer buyerId;
    private LocalDateTime orderTime;
    private String orderStatus;
    private BigDecimal transactionPrice;
}
