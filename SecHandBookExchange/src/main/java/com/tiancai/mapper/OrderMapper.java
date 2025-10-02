package com.tiancai.mapper;

import com.tiancai.dto.OrderDTO;
import com.tiancai.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    
    /**
     * 创建订单
     */
    void insertOrder(Order order);
    
    /**
     * 根据订单ID查询订单
     */
    Order findOrderById(Integer orderId);
    
    /**
     * 查询我作为买家的订单列表
     */
    List<OrderDTO> findOrdersByBuyerId(Integer buyerId);
    
    /**
     * 查询我作为卖家的订单列表
     */
    List<OrderDTO> findOrdersBySellerId(Integer sellerId);
    
    /**
     * 更新订单状态
     */
    void updateOrderStatus(@Param("orderId") Integer orderId, @Param("status") String status);
    
    /**
     * 调用存储过程完成交易
     */
    void completeTransaction(Map<String, Object> params);
}

