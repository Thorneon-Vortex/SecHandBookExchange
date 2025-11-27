package com.tiancai.service.impl;

import com.tiancai.dto.UserLoginDTO;
import com.tiancai.dto.UserRegisterDTO;
import com.tiancai.entity.User;
import com.tiancai.exception.BusinessException; // 假设有一个自定义异常
import com.tiancai.mapper.UserMapper;
import com.tiancai.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User register(UserRegisterDTO userRegisterDTO) {
        User existingUser = userMapper.getByStudentId(userRegisterDTO.getStudentId());
        if (existingUser != null) {
            throw new BusinessException(40901, "学号已被注册");
        }

        User user = new User();
        BeanUtils.copyProperties(userRegisterDTO, user);
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setCreditScore(100);
        user.setRegisterTime(LocalDateTime.now());
        userMapper.insert(user);
        
        return user;
    }

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String studentId = userLoginDTO.getStudentId();
        String password = userLoginDTO.getPassword();
        User user = userMapper.getByStudentId(studentId);

        if (user == null) {
            throw new BusinessException(401, "学号或密码错误");
        }
        
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException(401, "学号或密码错误");
        }

        return user;
    }
    
    @Override
    public User getUserInfoById(Integer id) {
        User user = userMapper.getById(id);
        if (user != null) {
            user.setPassword(null); // 安全处理，不返回密码
        }
        return user;
    }

    @Override
    public void updateUserInfo(User user) {
        userMapper.update(user);
    }
}