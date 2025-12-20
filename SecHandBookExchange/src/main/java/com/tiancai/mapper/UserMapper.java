package com.tiancai.mapper;

import com.tiancai.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;


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
    
    /**
     * 统计用户总数（B端用）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM user " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "WHERE student_id LIKE CONCAT('%', #{keyword}, '%') " +
            "OR nickname LIKE CONCAT('%', #{keyword}, '%') " +
            "</if>" +
            "</script>")
    Long countUsers(@Param("keyword") String keyword);
    
    /**
     * 分页查询用户列表（B端用）
     */
    @Select("<script>" +
            "SELECT * FROM user " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "WHERE student_id LIKE CONCAT('%', #{keyword}, '%') " +
            "OR nickname LIKE CONCAT('%', #{keyword}, '%') " +
            "</if>" +
            "ORDER BY register_time DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<User> findUsersWithPagination(@Param("offset") Integer offset, 
                                       @Param("pageSize") Integer pageSize,
                                       @Param("keyword") String keyword);
}