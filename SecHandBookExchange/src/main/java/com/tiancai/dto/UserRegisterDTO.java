package com.tiancai.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {
    private String studentId;
    private String nickname;
    private String password;
    private String contactInfo;
}