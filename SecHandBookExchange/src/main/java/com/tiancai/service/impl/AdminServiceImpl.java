package com.tiancai.service.impl;

import com.tiancai.dto.AdminLoginDTO;
import com.tiancai.entity.*;
import com.tiancai.exception.BusinessException;
import com.tiancai.mapper.AdminMapper;
import com.tiancai.mapper.UserMapper;
import com.tiancai.mapper.BookAndListingMapper;
import com.tiancai.mapper.OrderMapper;
import com.tiancai.service.AdminService;
import com.tiancai.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BookAndListingMapper bookAndListingMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Admin login(AdminLoginDTO adminLoginDTO) {
        // 1. 查找管理员
        Admin admin = adminMapper.findByUsername(adminLoginDTO.getUsername());
        if (admin == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 验证密码（简单MD5，实际应该用BCrypt）
        String password = DigestUtils.md5DigestAsHex(adminLoginDTO.getPassword().getBytes());
        if (!admin.getPassword().equals(password)) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 更新最后登录时间
        adminMapper.updateLastLoginTime(admin.getAdminId());

        return admin;
    }

    @Override
    public PageResult<User> getUserList(Integer page, Integer pageSize, String keyword) {
        // 计算偏移量
        int offset = (page - 1) * pageSize;
        
        // 查询总数
        long total = userMapper.countUsers(keyword);
        
        // 查询列表
        List<User> users = userMapper.findUsersWithPagination(offset, pageSize, keyword);
        
        PageResult<User> result = new PageResult<>();
        result.setTotal(total);
        result.setItems(users);
        result.setPage(page);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public void updateUserStatus(Integer userId, Boolean enabled) {
        // 这里可以添加状态字段，暂时只记录日志
        log.info("更新用户状态: userId={}, enabled={}", userId, enabled);
        // TODO: 如果User表有status字段，可以在这里更新
    }

    @Override
    public PageResult<com.tiancai.dto.AdminListingDTO> getListingList(Integer page, Integer pageSize, String keyword, String status) {
        int offset = (page - 1) * pageSize;
        
        // 查询总数
        long total = bookAndListingMapper.countListingsForAdmin(keyword, status);
        
        // 查询列表（包含书名信息）
        List<com.tiancai.dto.AdminListingDTO> listings = bookAndListingMapper.findListingsWithPaginationForAdmin(offset, pageSize, keyword, status);
        
        PageResult<com.tiancai.dto.AdminListingDTO> result = new PageResult<>();
        result.setTotal(total);
        result.setItems(listings);
        result.setPage(page);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public void takeDownListing(Integer listingId, String reason) {
        // 下架书籍（更新状态为"已下架"）
        bookAndListingMapper.updateListingStatus(listingId, "已下架");
        log.info("管理员下架书籍: listingId={}, reason={}", listingId, reason);
    }

    @Override
    public PageResult<Order> getOrderList(Integer page, Integer pageSize, String status) {
        int offset = (page - 1) * pageSize;
        
        // 查询总数
        long total = orderMapper.countOrders(status);
        
        // 查询列表
        List<Order> orders = orderMapper.findOrdersWithPagination(offset, pageSize, status);
        
        PageResult<Order> result = new PageResult<>();
        result.setTotal(total);
        result.setItems(orders);
        result.setPage(page);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户总数
        long userCount = userMapper.countUsers(null);
        stats.put("userCount", userCount);
        
        // 书籍总数
        long listingCount = bookAndListingMapper.countListingsForAdmin(null, null);
        stats.put("listingCount", listingCount);
        
        // 在售书籍数
        long onSaleCount = bookAndListingMapper.countListingsForAdmin(null, "在售");
        stats.put("onSaleCount", onSaleCount);
        
        // 订单总数
        long orderCount = orderMapper.countOrders(null);
        stats.put("orderCount", orderCount);
        
        // 已完成订单数
        long completedOrderCount = orderMapper.countOrders("已完成");
        stats.put("completedOrderCount", completedOrderCount);
        
        // 今日新增用户（需要UserMapper支持）
        stats.put("todayNewUsers", 0);
        
        // 今日新增订单（需要OrderMapper支持）
        stats.put("todayNewOrders", 0);
        
        return stats;
    }
}

