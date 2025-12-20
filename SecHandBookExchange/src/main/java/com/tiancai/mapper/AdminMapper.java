package com.tiancai.mapper;

import com.tiancai.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMapper {
    
    /**
     * 根据用户名查找管理员
     */
    Admin findByUsername(@Param("username") String username);
    
    /**
     * 根据ID查找管理员
     */
    Admin findById(@Param("adminId") Integer adminId);
    
    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(@Param("adminId") Integer adminId);
}


