package com.tiancai.service.impl;

import com.tiancai.dto.OrderCreateDTO;
import com.tiancai.dto.OrderDTO;
import com.tiancai.entity.Listing;
import com.tiancai.entity.Order;
import com.tiancai.exception.BusinessException;
import com.tiancai.exception.ForbiddenException;
import com.tiancai.exception.ResourceNotFoundException;
import com.tiancai.mapper.BookAndListingMapper;
import com.tiancai.mapper.OrderMapper;
import com.tiancai.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private BookAndListingMapper listingMapper;
    
    @Override
    @Transactional
    public Integer createOrder(OrderCreateDTO orderCreateDTO, Integer buyerId) {
        // 1. 查询发布信息
        Listing listing = listingMapper.findById(orderCreateDTO.getListingId());
        
        if (listing == null) {
            throw new ResourceNotFoundException("发布信息不存在");
        }
        
        // 2. 检查状态是否为"在售"
        if (!"在售".equals(listing.getStatus())) {
            throw new BusinessException(400, "书籍状态不是'在售'，无法下单");
        }
        
        // 3. 不能购买自己的书
        if (listing.getSellerId().equals(buyerId)) {
            throw new BusinessException(400, "不能购买自己发布的书籍");
        }
        
        // 4. 创建订单
        Order order = new Order();
        order.setListingId(orderCreateDTO.getListingId());
        order.setBuyerId(buyerId);
        order.setTransactionPrice(listing.getPrice());
        order.setOrderStatus("待确认");
        
        orderMapper.insertOrder(order);
        
        // 5. 更新发布信息状态为"已预定"（触发器会自动完成，但这里手动更新确保一致性）
        listingMapper.updateStatus(orderCreateDTO.getListingId(), "已预定");
        
        return order.getOrderId();
    }
    
    @Override
    public List<OrderDTO> getMyOrders(Integer userId, String role) {
        if ("seller".equals(role)) {
            return orderMapper.findOrdersBySellerId(userId);
        } else {
            // 默认为buyer
            return orderMapper.findOrdersByBuyerId(userId);
        }
    }
    
    @Override
    @Transactional
    public void completeOrder(Integer orderId, Integer currentUserId) {
        // 1. 查询订单
        Order order = orderMapper.findOrderById(orderId);
        
        if (order == null) {
            throw new ResourceNotFoundException("订单不存在");
        }
        
        // 2. 查询发布信息，获取卖家ID
        Listing listing = listingMapper.findById(order.getListingId());
        
        if (listing == null) {
            throw new ResourceNotFoundException("发布信息不存在");
        }
        
        // 3. 检查当前用户是否是订单相关方（买家或卖家）
        if (!order.getBuyerId().equals(currentUserId) && !listing.getSellerId().equals(currentUserId)) {
            throw new ForbiddenException("无权操作此订单");
        }
        
        // 4. 检查订单状态
        if (!"待确认".equals(order.getOrderStatus())) {
            throw new BusinessException(400, "订单状态不正确，无法完成");
        }
        
        // 5. 使用存储过程完成交易（带降级处理）
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        
        try {
            orderMapper.completeTransaction(params);
            
            Integer resultCode = (Integer) params.get("resultCode");
            String resultMessage = (String) params.get("resultMessage");
            
            log.info("存储过程执行结果: code={}, message={}", resultCode, resultMessage);
            
            if (resultCode != null && resultCode != 0) {
                throw new BusinessException(400, resultMessage);
            }
        } catch (BusinessException e) {
            throw e; // 重新抛出业务异常
        } catch (Exception e) {
            log.warn("调用存储过程失败，使用直接SQL更新方式", e);
            // 降级处理：使用直接SQL更新
            orderMapper.updateOrderStatus(orderId, "已完成");
            listingMapper.updateStatus(order.getListingId(), "已售出");
            log.info("订单 {} 已完成（通过直接SQL）", orderId);
        }
    }
    
    @Override
    @Transactional
    public void cancelOrder(Integer orderId, Integer currentUserId) {
        // 1. 查询订单
        Order order = orderMapper.findOrderById(orderId);
        
        if (order == null) {
            throw new ResourceNotFoundException("订单不存在");
        }
        
        // 2. 查询发布信息，获取卖家ID
        Listing listing = listingMapper.findById(order.getListingId());
        
        if (listing == null) {
            throw new ResourceNotFoundException("发布信息不存在");
        }
        
        // 3. 检查当前用户是否是订单相关方（买家或卖家）
        if (!order.getBuyerId().equals(currentUserId) && !listing.getSellerId().equals(currentUserId)) {
            throw new ForbiddenException("无权操作此订单");
        }
        
        // 4. 检查订单状态
        if ("已完成".equals(order.getOrderStatus())) {
            throw new BusinessException(400, "订单已完成，无法取消");
        }
        
        if ("已取消".equals(order.getOrderStatus())) {
            throw new BusinessException(400, "订单已取消");
        }
        
        // 5. 更新订单状态为"已取消"
        orderMapper.updateOrderStatus(orderId, "已取消");
        
        // 6. 恢复发布信息状态为"在售"（触发器会自动完成，但这里手动更新确保一致性）
        listingMapper.updateStatus(order.getListingId(), "在售");
    }
}

