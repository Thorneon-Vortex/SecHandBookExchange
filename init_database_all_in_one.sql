-- =====================================================
-- 校园二手书交易平台 - 数据库完整初始化脚本
-- =====================================================
-- 功能：一键创建所有表、触发器、存储过程和测试数据
-- 使用方法：
--   mysql -u root -pWsf197107 < init_database_all_in_one.sql
-- 或者在MySQL客户端中直接复制粘贴执行
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `sechandbookexchange` DEFAULT CHARACTER SET utf8mb4;
USE `sechandbookexchange`;

-- =====================================================
-- 第一部分：创建表结构
-- =====================================================

-- 删除已存在的表（按逆序）
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `listing`;
DROP TABLE IF EXISTS `book_category`;
DROP TABLE IF EXISTS `book`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `user`;

-- 1. 用户表
CREATE TABLE `user` (
  `user_id` INT NOT NULL AUTO_INCREMENT COMMENT '用户ID, 主键',
  `student_id` VARCHAR(20) NOT NULL COMMENT '学号, 唯一',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
  `password` VARCHAR(255) NOT NULL COMMENT '密码 (请务必存储加密后的哈希值)',
  `register_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `contact_info` VARCHAR(100) NULL COMMENT '联系方式 (如手机号或邮箱)',
  `credit_score` INT NOT NULL DEFAULT 100 COMMENT '信誉积分',
  PRIMARY KEY (`user_id`),
  UNIQUE INDEX `uk_student_id` (`student_id` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 书籍分类表
CREATE TABLE `category` (
  `category_id` INT NOT NULL AUTO_INCREMENT COMMENT '分类ID, 主键',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  PRIMARY KEY (`category_id`),
  UNIQUE INDEX `uk_category_name` (`category_name` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书籍分类表';

-- 3. 书籍信息表
CREATE TABLE `book` (
  `book_id` INT NOT NULL AUTO_INCREMENT COMMENT '书籍ID, 主键',
  `isbn` VARCHAR(20) NOT NULL COMMENT 'ISBN, 国际标准书号, 唯一',
  `title` VARCHAR(255) NOT NULL COMMENT '书名',
  `author` VARCHAR(100) NOT NULL COMMENT '作者',
  `publisher` VARCHAR(100) NULL COMMENT '出版社',
  `publication_year` VARCHAR(10) NULL COMMENT '出版年份',
  `cover_image_url` VARCHAR(512) NULL COMMENT '封面图片URL',
  PRIMARY KEY (`book_id`),
  UNIQUE INDEX `uk_isbn` (`isbn` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书籍基本信息表';

-- 4. 书籍与分类关联表
CREATE TABLE `book_category` (
  `book_id` INT NOT NULL COMMENT '书籍ID, 外键',
  `category_id` INT NOT NULL COMMENT '分类ID, 外键',
  PRIMARY KEY (`book_id`, `category_id`),
  INDEX `fk_book_category_category_idx` (`category_id` ASC),
  CONSTRAINT `fk_book_category_book`
    FOREIGN KEY (`book_id`)
    REFERENCES `book` (`book_id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_book_category_category`
    FOREIGN KEY (`category_id`)
    REFERENCES `category` (`category_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书籍与分类的关联表';

-- 5. 发布信息表
CREATE TABLE `listing` (
  `listing_id` INT NOT NULL AUTO_INCREMENT COMMENT '发布ID, 主键',
  `seller_id` INT NOT NULL COMMENT '发布者ID, 外键关联User',
  `book_id` INT NOT NULL COMMENT '书籍ID, 外键关联Book',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `condition_desc` VARCHAR(20) NOT NULL COMMENT '新旧程度',
  `listing_type` ENUM('出售', '赠送') NOT NULL COMMENT '发布类型: 出售/赠送',
  `status` ENUM('在售', '已预定', '已售出', '已下架') NOT NULL DEFAULT '在售' COMMENT '状态',
  `post_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `description` TEXT NULL COMMENT '详细描述',
  PRIMARY KEY (`listing_id`),
  INDEX `fk_listing_user_idx` (`seller_id` ASC),
  INDEX `fk_listing_book_idx` (`book_id` ASC),
  CONSTRAINT `fk_listing_user`
    FOREIGN KEY (`seller_id`)
    REFERENCES `user` (`user_id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_listing_book`
    FOREIGN KEY (`book_id`)
    REFERENCES `book` (`book_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书籍发布信息表';

-- 6. 订单表
CREATE TABLE `orders` (
  `order_id` INT NOT NULL AUTO_INCREMENT COMMENT '订单ID, 主键',
  `listing_id` INT NOT NULL COMMENT '发布ID, 外键关联Listing',
  `buyer_id` INT NOT NULL COMMENT '购买者ID, 外键关联User',
  `order_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `order_status` ENUM('待确认', '已完成', '已取消') NOT NULL DEFAULT '待确认' COMMENT '订单状态',
  `transaction_price` DECIMAL(10,2) NOT NULL COMMENT '交易价格',
  PRIMARY KEY (`order_id`),
  INDEX `fk_order_listing_idx` (`listing_id` ASC),
  INDEX `fk_order_user_idx` (`buyer_id` ASC),
  CONSTRAINT `fk_order_listing`
    FOREIGN KEY (`listing_id`)
    REFERENCES `listing` (`listing_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_order_user`
    FOREIGN KEY (`buyer_id`)
    REFERENCES `user` (`user_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单表';

-- 7. 评论表
CREATE TABLE `comment` (
  `comment_id` INT NOT NULL AUTO_INCREMENT COMMENT '评论ID, 主键',
  `listing_id` INT NOT NULL COMMENT '发布ID, 外键关联Listing',
  `user_id` INT NOT NULL COMMENT '评论者ID, 外键关联User',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `comment_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`comment_id`),
  INDEX `fk_comment_listing_idx` (`listing_id` ASC),
  INDEX `fk_comment_user_idx` (`user_id` ASC),
  CONSTRAINT `fk_comment_listing`
    FOREIGN KEY (`listing_id`)
    REFERENCES `listing` (`listing_id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comment_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`user_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论留言表';

SELECT '✅ 表结构创建成功！' AS 提示;

-- =====================================================
-- 第二部分：创建触发器
-- =====================================================

-- 删除已存在的触发器
DROP TRIGGER IF EXISTS after_order_insert;
DROP TRIGGER IF EXISTS after_order_update_complete;
DROP TRIGGER IF EXISTS after_order_update_cancel;

DELIMITER $$

-- 触发器1: 创建订单后更新书籍状态
CREATE TRIGGER after_order_insert
AFTER INSERT ON `orders`
FOR EACH ROW
BEGIN
    UPDATE `listing`
    SET `status` = '已预定'
    WHERE listing_id = NEW.listing_id;
END$$

-- 触发器2: 订单完成后增加用户信誉分
CREATE TRIGGER after_order_update_complete
AFTER UPDATE ON `orders`
FOR EACH ROW
BEGIN
    DECLARE seller_user_id INT;
    
    IF OLD.order_status != '已完成' AND NEW.order_status = '已完成' THEN
        SELECT seller_id INTO seller_user_id 
        FROM `listing` 
        WHERE listing_id = NEW.listing_id;
        
        UPDATE `user`
        SET credit_score = credit_score + 5
        WHERE user_id = NEW.buyer_id;
        
        UPDATE `user`
        SET credit_score = credit_score + 5
        WHERE user_id = seller_user_id;
    END IF;
END$$

-- 触发器3: 取消订单后恢复书籍状态
CREATE TRIGGER after_order_update_cancel
AFTER UPDATE ON `orders`
FOR EACH ROW
BEGIN
    IF NEW.order_status = '已取消' THEN
        UPDATE `listing`
        SET `status` = '在售'
        WHERE listing_id = NEW.listing_id;
    END IF;
END$$

DELIMITER ;

SELECT '✅ 触发器创建成功！' AS 提示;

-- =====================================================
-- 第三部分：创建存储过程
-- =====================================================

-- 删除已存在的存储过程
DROP PROCEDURE IF EXISTS complete_transaction;

DELIMITER $$

CREATE PROCEDURE complete_transaction(
    IN p_order_id INT,
    OUT p_result_code INT,
    OUT p_result_message VARCHAR(255)
)
BEGIN
    DECLARE current_order_status VARCHAR(50) DEFAULT NULL;
    DECLARE target_listing_id INT DEFAULT NULL;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
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
        START TRANSACTION;
        
        UPDATE `orders`
        SET order_status = '已完成'
        WHERE order_id = p_order_id;
        
        UPDATE `listing`
        SET `status` = '已售出'
        WHERE listing_id = target_listing_id;
        
        COMMIT;
        SET p_result_code = 0;
        SET p_result_message = '交易成功完成！';
    END IF;
END$$

DELIMITER ;

SELECT '✅ 存储过程创建成功！' AS 提示;

-- =====================================================
-- 第四部分：插入测试数据
-- =====================================================

-- 1. 插入测试分类
INSERT INTO `category` (category_name) VALUES 
('计算机'),
('文学小说'),
('外语学习'),
('经济管理'),
('数学'),
('物理'),
('历史'),
('艺术');

-- 2. 插入测试用户（密码都是123456的MD5）
INSERT INTO `user` (student_id, nickname, password, contact_info, credit_score) VALUES 
('21371001', '学霸小王', 'e10adc3949ba59abbe56e057f20f883e', 'QQ:123456', 100),
('21371002', '爱书的小李', 'e10adc3949ba59abbe56e057f20f883e', 'WeChat:abc123', 100),
('21371003', '书虫小张', 'e10adc3949ba59abbe56e057f20f883e', 'QQ:789012', 100);

-- 3. 插入测试书籍
INSERT INTO `book` (isbn, title, author, publisher, publication_year, cover_image_url) VALUES 
('9787111216719', '数据库系统概念', 'Abraham Silberschatz', '机械工业出版社', '2006', 'https://example.com/db.jpg'),
('9787115428028', 'Python编程：从入门到实践', 'Eric Matthes', '人民邮电出版社', '2016', 'https://example.com/python.jpg'),
('9787302476344', '算法导论', 'Thomas H. Cormen', '清华大学出版社', '2013', 'https://example.com/algo.jpg'),
('9787115468123', 'Java核心技术卷I', 'Cay S. Horstmann', '人民邮电出版社', '2018', 'https://example.com/java.jpg'),
('9787115279460', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', '2011', 'https://example.com/csapp.jpg');

-- 4. 关联书籍与分类
INSERT INTO `book_category` (book_id, category_id) VALUES 
(1, 1), -- 数据库系统概念 -> 计算机
(2, 1), -- Python -> 计算机
(3, 1), -- 算法导论 -> 计算机
(4, 1), -- Java -> 计算机
(5, 1); -- CSAPP -> 计算机

-- 5. 插入测试发布信息
INSERT INTO `listing` (seller_id, book_id, price, condition_desc, listing_type, status, description) VALUES 
(1, 1, 35.50, '九成新', '出售', '在售', '几乎全新，笔记很少，附赠复习资料。'),
(2, 2, 45.00, '全新', '出售', '在售', '全新未拆封，买错了。'),
(1, 3, 0.00, '八成新', '赠送', '在售', '已经学完了，送给需要的同学。'),
(3, 4, 55.00, '九成新', '出售', '在售', '看过一遍，保存得很好。'),
(2, 5, 68.00, '八成新', '出售', '在售', '有少量笔记，不影响阅读。');

SELECT '✅ 测试数据插入成功！' AS 提示;

-- =====================================================
-- 第五部分：验证创建结果
-- =====================================================

SELECT '========================================' AS '';
SELECT '📊 数据库初始化完成！' AS '';
SELECT '========================================' AS '';

-- 显示表统计
SELECT '📈 数据统计:' AS '';
SELECT 'user' AS 表名, COUNT(*) AS 记录数 FROM `user`
UNION ALL
SELECT 'category', COUNT(*) FROM `category`
UNION ALL
SELECT 'book', COUNT(*) FROM `book`
UNION ALL
SELECT 'listing', COUNT(*) FROM `listing`
UNION ALL
SELECT 'orders', COUNT(*) FROM `orders`;

-- 显示触发器
SELECT '⚡ 触发器列表:' AS '';
SHOW TRIGGERS;

-- 显示存储过程
SELECT '🔧 存储过程列表:' AS '';
SHOW PROCEDURE STATUS WHERE Db = 'sechandbookexchange';

SELECT '========================================' AS '';
SELECT '✅ 所有初始化步骤完成！' AS '';
SELECT '🚀 现在可以启动Spring Boot应用了' AS '';
SELECT '========================================' AS '';

