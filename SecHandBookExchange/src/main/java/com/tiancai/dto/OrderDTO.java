package com.tiancai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    
    private Integer orderId;
    private String orderStatus;
    private LocalDateTime orderTime;
    private BigDecimal transactionPrice;
    private String bookTitle;  // 书籍标题
    private String counterpartyNickname;  // 交易对方的昵称
}

