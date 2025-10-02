package com.tiancai.mapper;

import com.tiancai.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE student_id = #{studentId}")
    User getByStudentId(String studentId);

    @Insert("INSERT INTO user (student_id, nickname, password, contact_info, credit_score, register_time) " +
            "VALUES (#{studentId}, #{nickname}, #{password}, #{contactInfo}, #{creditScore}, #{registerTime})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    void insert(User user);

    @Select("SELECT * FROM user WHERE user_id = #{userId}")
    User getById(Integer userId);
    
    // 请确保数据库表中的主键列名为 user_id
    @Update("UPDATE user SET nickname = #{nickname}, contact_info = #{contactInfo} WHERE user_id = #{userId}")
    void update(User user);
}