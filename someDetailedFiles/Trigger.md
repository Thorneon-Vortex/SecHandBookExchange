### **Trigger**



*   **触发器 (Trigger)** 是 **数据库层面** 的功能。它是一种特殊的存储过程，由数据库管理系统（DBMS，如 MySQL/GaussDB）自己管理。你创建它一次，它就永久地驻留在数据库服务器上，**独立于任何应用程序（无论是 Java、Python 还是其他）**。当数据库中的某个表发生特定事件（如 `INSERT`, `UPDATE`, `DELETE`）时，数据库会自动“触发”并执行它。



一旦触发器被创建，无论你是用 MyBatis、JDBC 还是其他任何工具向 `Order` 表中插入或更新数据，数据库都会自动执行这些触发器。MyBatis 甚至根本不知道触发器的存在。

---

### **具体实现步骤**

三个触发器的完整 SQL 创建语句，以及如何应用到数据库中

#### **准备工作**

假设表结构如下（根据实际设计调整字段名）：

*   `listing` 表: `listing_id`, `seller_id`, `status` ('在售', '已预定', '已售出')
*   `order` 表: `order_id`, `listing_id`, `buyer_id`, `order_status` ('待确认', '已完成', '已取消')
*   `user` 表: `user_id`, `credit_score`

#### **Trigger 1: 创建订单后更新书籍状态**

当向 `order` 表插入一条新记录后，自动将 `listing` 表中对应书籍的状态更新为“已预定”。

```sql
-- 使用 DELIMITER 来定义一个代码块，因为触发器内部有分号
DELIMITER $$

CREATE TRIGGER after_order_insert
-- 在 order 表每次插入（INSERT）一条记录之后（AFTER）触发
AFTER INSERT ON `order`
FOR EACH ROW
BEGIN
    -- NEW 关键字代表刚刚插入的那一行数据
    -- 将 listing 表中，listing_id 等于新订单中 listing_id 的那条记录的状态更新
    UPDATE listing
    SET `status` = '已预定'
    WHERE listing_id = NEW.listing_id;
END$$

-- 将分隔符改回默认的分号
DELIMITER ;
```

#### **Trigger 2: 订单完成后增加用户信誉**

当 `order` 表中某条记录的状态被更新时，检查新状态是否为“已完成”，如果是，则为买家和卖家增加信誉分。

```sql
DELIMITER $$

CREATE TRIGGER after_order_update_complete
-- 在 order 表每次更新（UPDATE）一条记录之后（AFTER）触发
AFTER UPDATE ON `order`
FOR EACH ROW
BEGIN
    -- 声明一个变量来存储卖家的ID
    DECLARE seller_user_id INT;

    -- OLD 代表更新前的数据，NEW 代表更新后的数据
    -- 检查订单状态是否从“非完成”变成了“已完成”
    IF OLD.order_status != '已完成' AND NEW.order_status = '已完成' THEN
        -- 1. 根据新订单的 listing_id 从 listing 表中查出卖家的ID
        SELECT seller_id INTO seller_user_id FROM listing WHERE listing_id = NEW.listing_id;

        -- 2. 给买家增加信誉分
        UPDATE `user`
        SET credit_score = credit_score + 5
        WHERE user_id = NEW.buyer_id;

        -- 3. 给卖家增加信誉分
        UPDATE `user`
        SET credit_score = credit_score + 5
        WHERE user_id = seller_user_id;
    END IF;
END$$

DELIMITER ;
```

#### **Trigger 3: 取消订单后恢复书籍状态**

当 `order` 表中某条记录的状态被更新为“已取消”时，将对应 `listing` 的状态恢复为“在售”。

```sql
DELIMITER $$

CREATE TRIGGER after_order_update_cancel
-- 同样是在 order 表更新之后触发
AFTER UPDATE ON `order`
FOR EACH ROW
BEGIN
    -- 检查订单状态是否变为了“已取消”
    IF NEW.order_status = '已取消' THEN
        -- 将 listing 表中对应书籍的状态恢复为“在售”
        UPDATE listing
        SET `status` = '在售'
        WHERE listing_id = NEW.listing_id;
    END IF;
END$$

DELIMITER ;
```

---

### **如何执行这些 SQL (如何“应用”到数据库)**

你有三种推荐的方式来执行上面这些 `CREATE TRIGGER` 语句，**任选其一即可**。

#### **在数据库客户端中直接执行**

1.  打开数据库可视化工具，如 **Navicat**, **DataGrip**, 或者 MySQL/GaussDB 的命令行工具。
2.  连接到的项目数据库。
3.  打开一个“查询”或“SQL 编辑器”窗口。
4.  将上面提供的三段 `CREATE TRIGGER` SQL 代码**依次**复制粘贴进去，然后执行。

执行成功后，这些触发器就永久保存在数据库里了。MyBatis 应用在进行增删改查时，它们会自动生效。

