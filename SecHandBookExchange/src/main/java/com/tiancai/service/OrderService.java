package com.tiancai.service;

import com.tiancai.dto.OrderCreateDTO;
import com.tiancai.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    
    /**
     * 创建订单
     */
    Integer createOrder(OrderCreateDTO orderCreateDTO, Integer buyerId);
    
    /**
     * 获取我的订单列表
     * @param userId 用户ID
     * @param role buyer(买家) 或 seller(卖家)
     */
    List<OrderDTO> getMyOrders(Integer userId, String role);
    
    /**
     * 确认交易完成
     */
    void completeOrder(Integer orderId, Integer currentUserId);
    
    /**
     * 取消订单
     */
    void cancelOrder(Integer orderId, Integer currentUserId);
}

