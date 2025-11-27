package com.tiancai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    private Integer commentId;
    private Integer listingId;
    private Integer userId;
    private String content;
    private LocalDateTime commentTime;
}
