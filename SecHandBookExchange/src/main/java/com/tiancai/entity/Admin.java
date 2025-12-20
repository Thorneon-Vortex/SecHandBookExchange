package com.tiancai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    private Integer adminId;
    private String username;
    
    @JsonIgnore
    private String password;
    
    private String role;  // super_admin, admin, operator
    private LocalDateTime createdTime;
    private LocalDateTime lastLoginTime;
}


