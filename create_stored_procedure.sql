-- =====================================================
-- 校园二手书交易平台 - 存储过程创建脚本
-- =====================================================
-- 功能：创建 complete_transaction 存储过程
-- 用途：完成交易，更新订单和发布信息状态
-- 注意：执行前请确保已连接到正确的数据库
-- =====================================================

-- 1. 如果存储过程已存在，先删除
DROP PROCEDURE IF EXISTS complete_transaction;

-- 2. 修改分隔符
DELIMITER $$

-- 3. 创建存储过程
CREATE PROCEDURE complete_transaction(
    IN p_order_id INT,
    OUT p_result_code INT,
    OUT p_result_message VARCHAR(255)
)
BEGIN
    -- 声明变量
    DECLARE current_order_status VARCHAR(50) DEFAULT NULL;
    DECLARE target_listing_id INT DEFAULT NULL;

    -- 声明一个退出处理器，用于捕获SQL异常
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        -- 如果发生错误，回滚事务并设置错误信息
        ROLLBACK;
        SET p_result_code = 2;
        SET p_result_message = '操作失败：发生未知数据库错误。';
    END;

    -- 查询订单信息（修复：不使用COUNT，直接查询字段）
    SELECT order_status, listing_id 
    INTO current_order_status, target_listing_id
    FROM `orders` 
    WHERE order_id = p_order_id
    LIMIT 1;

    -- 检查订单是否存在（通过判断是否查询到数据）
    IF current_order_status IS NULL THEN
        SET p_result_code = 1;
        SET p_result_message = '操作失败：订单不存在。';
    ELSEIF current_order_status != '待确认' THEN
        SET p_result_code = 1;
        SET p_result_message = CONCAT('操作失败：订单当前状态为 "', current_order_status, '"，无法完成。');
    ELSE
        -- 开启事务
        START TRANSACTION;

        -- 1. 更新订单状态为"已完成"
        UPDATE `orders`
        SET order_status = '已完成'
        WHERE order_id = p_order_id;

        -- 2. 更新发布信息状态为"已售出"
        UPDATE `listing`
        SET `status` = '已售出'
        WHERE listing_id = target_listing_id;
        
        -- 3. 信誉分更新由触发器自动完成，此处无需代码

        -- 提交事务
        COMMIT;
        SET p_result_code = 0;
        SET p_result_message = '交易成功完成！';
    END IF;
END$$

-- 4. 恢复分隔符
DELIMITER ;

-- =====================================================
-- 测试存储过程
-- =====================================================
-- 使用示例（在创建存储过程后执行）：
-- CALL complete_transaction(1, @result_code, @result_message);
-- SELECT @result_code AS 结果代码, @result_message AS 结果消息;
-- 
-- 返回码说明：
-- 0 = 成功
-- 1 = 订单不存在或状态不正确
-- 2 = 数据库错误
-- =====================================================

-- 显示创建成功的消息
SELECT '存储过程 complete_transaction 创建成功！' AS 提示;

