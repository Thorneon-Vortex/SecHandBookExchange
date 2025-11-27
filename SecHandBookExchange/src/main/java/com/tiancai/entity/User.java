package com.tiancai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data // 自动生成 Getter, Setter, toString, equals, hashCode
@NoArgsConstructor // 自动生成无参构造函数
@AllArgsConstructor // 自动生成全参构造函数
public class User {

    private Integer userId;
    private String studentId;
    private String nickname;
    
    @JsonIgnore  // 密码字段不应该在API响应中返回
    private String password;
    
    private LocalDateTime registerTime;
    private String contactInfo;
    private Integer creditScore;
}
