package com.tiancai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {
    
    private Integer listingId;  // 想要购买的书籍发布ID
}

