
和触发器一样，存储过程也是在**数据库层面**创建和存储的，Java/MyBatis 应用只是去“调用”（CALL）它而已。使用原生 SQL 来创建它们。

---

### **存储过程 1: 用户注册流程 (register_user)**

**目标：** 安全地创建一个新用户。在插入新用户前，检查学号是否已存在，以保证学号的唯一性，并返回操作结果。

**设计思路：**
1.  **输入参数 (IN)**：需要客户端（Java应用）传入新用户的基本信息，如学号 `p_student_id`、昵称 `p_nickname`、密码 `p_password` 和联系方式 `p_contact_info`。（参数前加 `p_` 是一种好习惯，用于区分列名）。
2.  **输出参数 (OUT)**：需要一个方式来告诉调用者操作是否成功以及原因。我们使用输出参数 `p_result_code` (整数) 和 `p_result_message` (字符串) 来实现。
    *   `p_result_code = 0` 表示成功。
    *   `p_result_code = 1` 表示学号已存在。
    *   `p_result_code = 2` 表示其他错误。
3.  **内部逻辑**：
    *   声明一个变量 `user_count` 用于计数。
    *   查询 `user` 表中是否存在与输入学号相同的记录，并将数量存入 `user_count`。
    *   使用 `IF-ELSE` 语句判断 `user_count` 的值：
        *   如果 `user_count > 0`，说明学号已存在，设置返回码为 1。
        *   如果 `user_count = 0`，执行 `INSERT` 操作插入新用户，并设置返回码为 0。

#### **SQL 实现 (在数据库客户端执行)**

```sql
DELIMITER $$

CREATE PROCEDURE register_user(
    -- 输入参数
    IN p_student_id VARCHAR(255),
    IN p_nickname VARCHAR(255),
    IN p_password VARCHAR(255),
    IN p_contact_info VARCHAR(255),
    -- 输出参数
    OUT p_result_code INT,
    OUT p_result_message VARCHAR(255)
)
BEGIN
    -- 声明一个局部变量来存储查询到的用户数量
    DECLARE user_count INT DEFAULT 0;

    -- 检查学号是否已经存在
    SELECT COUNT(*) INTO user_count FROM `user` WHERE student_id = p_student_id;

    -- 根据检查结果执行不同操作
    IF user_count > 0 THEN
        -- 学号已存在，设置错误信息
        SET p_result_code = 1;
        SET p_result_message = '注册失败：该学号已被注册。';
    ELSE
        -- 学号不存在，可以进行注册
        -- 使用 TRY-CATCH 结构来捕获可能的插入错误 (需要MySQL/GaussDB版本支持)
        BEGIN
            -- 声明一个退出处理器，用于捕获SQL异常
            DECLARE EXIT HANDLER FOR SQLEXCEPTION
            BEGIN
                -- 如果发生错误，回滚事务并设置错误信息
                ROLLBACK;
                SET p_result_code = 2;
                SET p_result_message = '注册失败：发生未知数据库错误。';
            END;

            -- 开启事务
            START TRANSACTION;
            
            -- 插入新用户记录
            INSERT INTO `user` (student_id, nickname, `password`, contact_info, register_time, credit_score)
            VALUES (p_student_id, p_nickname, p_password, p_contact_info, NOW(), 100); -- 假设初始信誉分为100

            -- 如果插入成功，提交事务并设置成功信息
            COMMIT;
            SET p_result_code = 0;
            SET p_result_message = '注册成功！';
        END;
    END IF;
END$$

DELIMITER ;
```

---

### **存储过程 2: 完成交易流程 (complete_transaction)**

**目标：** 将一个订单标记为“已完成”，并确保所有相关的状态更新（如书籍状态）都在一个原子操作中完成。这非常适合使用事务。

**设计思路：**
1.  **输入参数 (IN)**：只需要 `p_order_id`，即要完成的订单的ID。
2.  **输出参数 (OUT)**：同样使用 `p_result_code` 和 `p_result_message` 来返回操作结果。
    *   `p_result_code = 0`: 成功
    *   `p_result_code = 1`: 订单不存在或状态不正确
    *   `p_result_code = 2`: 数据库错误
3.  **内部逻辑**：
    *   **事务管理**：整个过程必须在一个事务中执行。
    *   **状态验证**：首先检查订单是否存在，并且其当前状态是 `待确认` 或 `已预定`（取决于你的业务流程，这里假设为 `待确认`）。如果状态不正确，则不能执行完成操作。
    *   **核心操作**：
        *   更新 `order` 表的状态为 `已完成`。
        *   根据订单中的 `listing_id`，更新 `listing` 表的状态为 `已售出`。
        *   (注意：信誉积分的增加由我们之前创建的 `after_order_update_complete` **触发器自动完成**，所以存储过程中不需要再写这部分逻辑，这体现了职责分离)。
    *   **错误处理**：如果任何一步失败，整个事务需要回滚（`ROLLBACK`），保证数据的一致性。

#### **SQL 实现 (在数据库客户端执行)**

```sql
DELIMITER $$

CREATE PROCEDURE complete_transaction(
    IN p_order_id INT,
    OUT p_result_code INT,
    OUT p_result_message VARCHAR(255)
)
BEGIN
    -- 声明变量
    DECLARE current_order_status VARCHAR(50);
    DECLARE target_listing_id INT;
    DECLARE order_exists INT DEFAULT 0;

    -- 声明一个退出处理器，用于捕获SQL异常
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        -- 如果发生错误，回滚事务并设置错误信息
        ROLLBACK;
        SET p_result_code = 2;
        SET p_result_message = '操作失败：发生未知数据库错误。';
    END;

    -- 查询订单的当前状态和是否存在
    SELECT COUNT(*), order_status, listing_id 
    INTO order_exists, current_order_status, target_listing_id 
    FROM `order` 
    WHERE order_id = p_order_id;

    -- 检查订单是否存在以及状态是否正确
    IF order_exists = 0 THEN
        SET p_result_code = 1;
        SET p_result_message = '操作失败：订单不存在。';
    ELSEIF current_order_status != '待确认' THEN -- 或者你的状态是'已预定'
        SET p_result_code = 1;
        SET p_result_message = CONCAT('操作失败：订单当前状态为 "', current_order_status, '"，无法完成。');
    ELSE
        -- 开启事务
        START TRANSACTION;

        -- 1. 更新订单状态为“已完成”
        UPDATE `order`
        SET order_status = '已完成'
        WHERE order_id = p_order_id;

        -- 2. 更新发布信息状态为“已售出”
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

DELIMITER ;
```

---

### **如何在 MyBatis 中调用存储过程**

现在，你已经在数据库中创建了这两个存储过程。接下来是在你的 MyBatis Mapper 中定义如何去调用它们。

#### **Mapper XML 配置**

在你的 `UserMapper.xml` (用于注册) 和 `OrderMapper.xml` (用于完成交易) 中添加如下配置。

**1. 调用 `register_user`**

```xml
<select id="registerUser" statementType="CALLABLE" parameterType="map">
    {
        CALL register_user(
            #{studentId, mode=IN, jdbcType=VARCHAR},
            #{nickname, mode=IN, jdbcType=VARCHAR},
            #{password, mode=IN, jdbcType=VARCHAR},
            #{contactInfo, mode=IN, jdbcType=VARCHAR},
            #{resultCode, mode=OUT, jdbcType=INTEGER},
            #{resultMessage, mode=OUT, jdbcType=VARCHAR}
        )
    }
</select>
```

**2. 调用 `complete_transaction`**

```xml
<update id="completeTransaction" statementType="CALLABLE" parameterType="map">
    {
        CALL complete_transaction(
            #{orderId, mode=IN, jdbcType=INTEGER},
            #{resultCode, mode=OUT, jdbcType=INTEGER},
            #{resultMessage, mode=OUT, jdbcType=VARCHAR}
        )
    }
</update>
```

**关键点解释**：
*   `statementType="CALLABLE"`: 明确告诉 MyBatis 这是一条调用存储过程的语句。
*   `parameterType="map"`: 我们使用 `Map` 来传递参数，因为它能方便地同时处理输入和输出参数。
*   `mode=IN`/`mode=OUT`: 指定每个参数是输入还是输出。
*   `jdbcType`: 明确指定参数的 JDBC 类型，这对于存储过程调用是个好习惯。

#### **Java 代码（Service/Mapper 接口）**

**Mapper 接口定义:**
```java
// 在 UserMapper.java 中
void registerUser(Map<String, Object> params);

// 在 OrderMapper.java 中
void completeTransaction(Map<String, Object> params);
```

**Service 层调用示例:**
```java
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    public String register(User user) {
        Map<String, Object> params = new HashMap<>();
        params.put("studentId", user.getStudentId());
        params.put("nickname", user.getNickname());
        params.put("password", user.getPassword()); // 应该加密处理
        params.put("contactInfo", user.getContactInfo());
        // 输出参数会被MyBatis自动填充回来
        params.put("resultCode", null);
        params.put("resultMessage", null);

        userMapper.registerUser(params);

        // 从 Map 中获取输出参数的值
        Integer resultCode = (Integer) params.get("resultCode");
        String resultMessage = (String) params.get("resultMessage");

        System.out.println("Result Code: " + resultCode);
        System.out.println("Result Message: " + resultMessage);
        
        return resultMessage; // 返回给Controller
    }
}
```

这样，完成了一个完整的闭环：在数据库中创建健壮的存储过程，然后在 MyBatis 应用中安全地调用它们。这种方式极大地增强了业务逻辑的内聚性和数据的安全性。