package com.tiancai.service;

import com.tiancai.dto.AdminLoginDTO;
import com.tiancai.entity.Admin;
import com.tiancai.entity.User;
import com.tiancai.entity.PageResult;
import com.tiancai.entity.Order;

import java.util.Map;

/**
 * 管理员服务接口
 */
public interface AdminService {
    
    /**
     * 管理员登录
     */
    Admin login(AdminLoginDTO adminLoginDTO);
    
    /**
     * 获取用户列表（分页）
     */
    PageResult<User> getUserList(Integer page, Integer pageSize, String keyword);
    
    /**
     * 更新用户状态（启用/禁用）
     */
    void updateUserStatus(Integer userId, Boolean enabled);
    
    /**
     * 获取书籍列表（分页）
     */
    PageResult<com.tiancai.dto.AdminListingDTO> getListingList(Integer page, Integer pageSize, String keyword, String status);
    
    /**
     * 下架书籍
     */
    void takeDownListing(Integer listingId, String reason);
    
    /**
     * 获取订单列表（分页）
     */
    PageResult<Order> getOrderList(Integer page, Integer pageSize, String status);
    
    /**
     * 获取数据统计
     */
    Map<String, Object> getStatistics();
}

