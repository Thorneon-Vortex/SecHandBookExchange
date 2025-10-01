package com.tiancai.dto;

import lombok.Data;

@Data
public class SellerDTO {
    private Integer userId;
    private String nickname;
    private Integer creditScore;
    private String contactInfo;
}
