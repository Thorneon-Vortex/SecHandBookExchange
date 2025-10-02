package com.tiancai.controller;

import com.tiancai.dto.OrderCreateDTO;
import com.tiancai.dto.OrderDTO;
import com.tiancai.entity.Result;
import com.tiancai.service.OrderService;
import com.tiancai.utils.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@Slf4j
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 3.1 创建订单
     */
    @PostMapping
    public Result createOrder(@RequestBody OrderCreateDTO orderCreateDTO) {
        Integer currentUserId = BaseContext.getCurrentId();
        log.info("用户 {} 创建订单，listingId: {}", currentUserId, orderCreateDTO.getListingId());
        
        try {
            Integer orderId = orderService.createOrder(orderCreateDTO, currentUserId);
            return Result.success(Map.of("orderId", orderId));
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 3.2 获取我的订单列表
     */
    @GetMapping
    public Result getMyOrders(@RequestParam(defaultValue = "buyer") String role) {
        Integer currentUserId = BaseContext.getCurrentId();
        log.info("用户 {} 查询订单列表，角色: {}", currentUserId, role);
        
        try {
            List<OrderDTO> orders = orderService.getMyOrders(currentUserId, role);
            return Result.success(orders);
        } catch (Exception e) {
            log.error("查询订单列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 3.3 确认交易完成
     */
    @PutMapping("/{id}/complete")
    public Result completeOrder(@PathVariable Integer id) {
        Integer currentUserId = BaseContext.getCurrentId();
        log.info("用户 {} 确认交易完成，订单ID: {}", currentUserId, id);
        
        try {
            orderService.completeOrder(id, currentUserId);
            return Result.success("交易已成功完成！");
        } catch (Exception e) {
            log.error("确认交易失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 3.4 取消订单
     */
    @PutMapping("/{id}/cancel")
    public Result cancelOrder(@PathVariable Integer id) {
        Integer currentUserId = BaseContext.getCurrentId();
        log.info("用户 {} 取消订单，订单ID: {}", currentUserId, id);
        
        try {
            orderService.cancelOrder(id, currentUserId);
            return Result.success("订单已取消");
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return Result.error(e.getMessage());
        }
    }
}

