package com.tiancai.service;

import com.tiancai.dto.UserLoginDTO;
import com.tiancai.dto.UserRegisterDTO;
import com.tiancai.entity.User;

public interface UserService {
    User register(UserRegisterDTO userRegisterDTO);
    User login(UserLoginDTO userLoginDTO);
    User getUserInfoById(Integer id);
    void updateUserInfo(User user);
}