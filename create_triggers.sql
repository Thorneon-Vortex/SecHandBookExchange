-- =====================================================
-- 校园二手书交易平台 - 触发器创建脚本
-- =====================================================
-- 功能：创建三个触发器
--   1. after_order_insert - 创建订单后更新书籍状态为"已预定"
--   2. after_order_update_complete - 订单完成后增加买卖双方信誉分
--   3. after_order_update_cancel - 取消订单后恢复书籍状态为"在售"
-- 注意：执行前请确保已连接到正确的数据库
-- =====================================================

-- 如果触发器已存在，先删除
DROP TRIGGER IF EXISTS after_order_insert;
DROP TRIGGER IF EXISTS after_order_update_complete;
DROP TRIGGER IF EXISTS after_order_update_cancel;

-- =====================================================
-- Trigger 1: 创建订单后更新书籍状态
-- =====================================================
-- 功能：当新建订单时，自动将对应的listing状态更新为"已预定"

DELIMITER $$

CREATE TRIGGER after_order_insert
AFTER INSERT ON `orders`
FOR EACH ROW
BEGIN
    -- NEW 关键字代表刚刚插入的那一行数据
    -- 将 listing 表中，listing_id 等于新订单中 listing_id 的那条记录的状态更新为"已预定"
    UPDATE `listing`
    SET `status` = '已预定'
    WHERE listing_id = NEW.listing_id;
END$$

DELIMITER ;

SELECT '触发器 after_order_insert 创建成功！' AS 提示;

-- =====================================================
-- Trigger 2: 订单完成后增加用户信誉分
-- =====================================================
-- 功能：当订单状态变为"已完成"时，给买家和卖家各增加5分信誉分

DELIMITER $$

CREATE TRIGGER after_order_update_complete
AFTER UPDATE ON `orders`
FOR EACH ROW
BEGIN
    -- 声明一个变量来存储卖家的ID
    DECLARE seller_user_id INT;

    -- OLD 代表更新前的数据，NEW 代表更新后的数据
    -- 检查订单状态是否从"非完成"变成了"已完成"
    IF OLD.order_status != '已完成' AND NEW.order_status = '已完成' THEN
        -- 1. 根据新订单的 listing_id 从 listing 表中查出卖家的ID
        SELECT seller_id INTO seller_user_id 
        FROM `listing` 
        WHERE listing_id = NEW.listing_id;

        -- 2. 给买家增加信誉分（+5分）
        UPDATE `user`
        SET credit_score = credit_score + 5
        WHERE user_id = NEW.buyer_id;

        -- 3. 给卖家增加信誉分（+5分）
        UPDATE `user`
        SET credit_score = credit_score + 5
        WHERE user_id = seller_user_id;
    END IF;
END$$

DELIMITER ;

SELECT '触发器 after_order_update_complete 创建成功！' AS 提示;

-- =====================================================
-- Trigger 3: 取消订单后恢复书籍状态
-- =====================================================
-- 功能：当订单状态变为"已取消"时，将对应的listing状态恢复为"在售"

DELIMITER $$

CREATE TRIGGER after_order_update_cancel
AFTER UPDATE ON `orders`
FOR EACH ROW
BEGIN
    -- 检查订单状态是否变为了"已取消"
    IF NEW.order_status = '已取消' THEN
        -- 将 listing 表中对应书籍的状态恢复为"在售"
        UPDATE `listing`
        SET `status` = '在售'
        WHERE listing_id = NEW.listing_id;
    END IF;
END$$

DELIMITER ;

SELECT '触发器 after_order_update_cancel 创建成功！' AS 提示;

-- =====================================================
-- 验证触发器创建成功
-- =====================================================
SELECT '所有触发器创建完成！' AS 提示;

-- 查看所有触发器
SHOW TRIGGERS;

-- =====================================================
-- 测试说明
-- =====================================================
-- 1. 测试 after_order_insert：
--    创建一个订单，查看对应的listing状态是否变为"已预定"
--
-- 2. 测试 after_order_update_complete：
--    将订单状态更新为"已完成"，查看买卖双方的credit_score是否各增加了5分
--
-- 3. 测试 after_order_update_cancel：
--    将订单状态更新为"已取消"，查看对应的listing状态是否恢复为"在售"
-- =====================================================

