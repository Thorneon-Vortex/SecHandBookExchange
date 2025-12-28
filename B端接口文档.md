这份文档是为管理后台前端开发者设计的，描述了管理端（B端）的所有 API 接口。文档将遵循 OpenAPI (Swagger) 的常用规范格式。

---

### **校园二手书交易与分享平台 - B端（管理端）API 接口文档 (V1.0)**

#### **1. 项目概述**

*   **项目名称**: 校园二手书交易与分享平台 - 管理后台
*   **基础 URL (Base URL)**: `https://api.yourdomain.com/v1`
*   **数据格式**: 所有请求和响应的主体均为 `application/json` 格式。

#### **2. 认证 (Authentication)**

*   管理端采用 **JWT (JSON Web Token)** 进行认证。
*   管理员通过登录接口 (`/admin/auth/login`) 获取 Token。
*   Token中包含特殊标识 `type: "admin"` 用于区分管理员与普通用户。
*   对于所有 `/admin/*` 路径下的接口（除登录外），客户端必须在 HTTP 请求头 (Header) 中携带 `Authorization` 字段，格式为 `Bearer <your_jwt_token>`。
*   未提供 Token 或 Token 无效/过期的请求将返回 `401 Unauthorized` 错误。
*   非管理员Token访问管理端接口将返回 `403 Forbidden` 错误。

#### **3. 标准响应格式**

为了便于客户端统一处理，所有 API 响应都遵循以下结构：

*   **成功响应 (HTTP Status: 200, 201)**
    ```json
    {
      "code": 1,
      "msg": "success",
      "data": { ... } // `data` 字段包含具体返回的数据，可能为对象或数组
    }
    ```
*   **失败响应 (HTTP Status: 4xx, 5xx)**
    ```json
    {
      "code": 0,
      "msg": "具体的错误信息，如：参数格式不正确",
      "data": null
    }
    ```

---

### **接口详情**

### **模块一：认证模块 (Authentication Module)**

#### **1.1 管理员登录**
*   **功能**: 管理员登录以获取认证 Token。
*   **Endpoint**: `POST /admin/auth/login`
*   **认证**: 无需
*   **请求体 (Request Body)**:
    ```json
    {
      "username": "admin",      // String, 必填, 管理员用户名
      "password": "admin123"    // String, 必填, 管理员密码
    }
    ```
*   **成功响应 (200 OK)**:
    ```json
    {
      "code": 1,
      "msg": "success",
      "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "adminId": 1,
        "username": "admin",
        "role": "super_admin"  // 角色: super_admin(超级管理员), admin(普通管理员)
      }
    }
    ```
*   **失败响应**:
    *   `400 Bad Request`: 参数缺失。
    *   `401 Unauthorized`: 用户名或密码错误。

---

### **模块二：数据统计模块 (Dashboard Module)**

#### **2.1 获取数据统计**
*   **功能**: 获取平台核心数据统计信息，用于管理后台首页展示。
*   **Endpoint**: `GET /admin/dashboard/statistics`
*   **认证**: **需要**
*   **成功响应 (200 OK)**:
    ```json
    {
      "code": 1,
      "msg": "success",
      "data": {
        "totalUsers": 1256,           // 总用户数
        "totalListings": 3842,        // 总发布数
        "totalOrders": 2156,          // 总订单数
        "todayNewUsers": 15,          // 今日新增用户
        "todayNewListings": 48,       // 今日新增发布
        "todayNewOrders": 32,         // 今日新增订单
        "activeListings": 1520,       // 在售书籍数
        "completedOrders": 1890,      // 已完成订单数
        "totalTransactionAmount": 45680.50  // 总交易金额
      }
    }
    ```

---

### **模块三：用户管理模块 (User Management Module)**

#### **3.1 获取用户列表**
*   **功能**: 分页获取平台所有用户列表，支持关键词搜索。
*   **Endpoint**: `GET /admin/users`
*   **认证**: **需要**
*   **查询参数 (Query Parameters)**:
    *   `page` (Integer, 可选, 默认1): 页码。
    *   `pageSize` (Integer, 可选, 默认10): 每页数量。
    *   `keyword` (String, 可选): 搜索关键词，匹配学号、昵称。
*   **成功响应 (200 OK)**:
    ```json
    {
      "code": 1,
      "msg": "success",
      "data": {
        "total": 1256,
        "items": [
          {
            "userId": 101,
            "studentId": "21371001",
            "nickname": "爱书的同学",
            "contactInfo": "QQ:123456",
            "creditScore": 105,
            "registerTime": "2025-09-10T10:00:00",
            "enabled": true    // 用户状态: true-正常, false-禁用
          }
        ]
      }
    }
    ```

#### **3.2 更新用户状态**
*   **功能**: 启用或禁用用户账户。
*   **Endpoint**: `PUT /admin/users/{userId}/status`
*   **认证**: **需要**
*   **路径参数 (Path Variable)**:
    *   `userId` (Integer, 必填): 用户ID。
*   **查询参数 (Query Parameters)**:
    *   `enabled` (Boolean, 必填): 目标状态。`true`-启用，`false`-禁用。
*   **成功响应 (200 OK)**:
    ```json
    {
      "code": 1,
      "msg": "success",
      "data": "用户状态更新成功"
    }
    ```
*   **失败响应**:
    *   `404 Not Found`: 用户不存在。
*   **使用场景**: 
    *   禁用违规用户账户
    *   恢复被误禁用的用户

---

### **模块四：书籍管理模块 (Listing Management Module)**

#### **4.1 获取书籍列表**
*   **功能**: 分页获取平台所有书籍发布列表，支持关键词搜索和状态筛选。
*   **Endpoint**: `GET /admin/listings`
*   **认证**: **需要**
*   **查询参数 (Query Parameters)**:
    *   `page` (Integer, 可选, 默认1): 页码。
    *   `pageSize` (Integer, 可选, 默认10): 每页数量。
    *   `keyword` (String, 可选): 搜索关键词，匹配书名、作者、ISBN。
    *   `status` (String, 可选): 状态筛选。可选值: `在售`, `已预定`, `已售出`, `已下架`。
*   **成功响应 (200 OK)**:
    ```json
    {
      "code": 1,
      "msg": "success",
      "data": {
        "total": 3842,
        "items": [
          {
            "listingId": 1,
            "title": "数据库系统概念",
            "author": "Abraham Silberschatz",
            "isbn": "9787111216719",
            "price": 35.50,
            "condition": "九成新",
            "listingType": "出售",
            "status": "在售",
            "postTime": "2025-09-12T09:30:00",
            "sellerNickname": "学霸",
            "sellerId": 102,
            "coverImageUrl": "https://images.example.com/db.jpg"
          }
        ]
      }
    }
    ```

#### **4.2 下架书籍**
*   **功能**: 管理员强制下架违规或不当的书籍发布。
*   **Endpoint**: `PUT /admin/listings/{listingId}/take-down`
*   **认证**: **需要**
*   **路径参数 (Path Variable)**:
    *   `listingId` (Integer, 必填): 发布ID。
*   **查询参数 (Query Parameters)**:
    *   `reason` (String, 可选, 默认"管理员下架"): 下架原因。
*   **成功响应 (200 OK)**:
    ```json
    {
      "code": 1,
      "msg": "success",
      "data": "书籍已下架"
    }
    ```
*   **失败响应**:
    *   `404 Not Found`: 发布信息不存在。
*   **使用场景**: 
    *   下架违规书籍（如盗版、不当内容）
    *   处理用户投诉

---

### **模块五：订单管理模块 (Order Management Module)**

#### **5.1 获取订单列表**
*   **功能**: 分页获取平台所有订单列表，支持状态筛选。
*   **Endpoint**: `GET /admin/orders`
*   **认证**: **需要**
*   **查询参数 (Query Parameters)**:
    *   `page` (Integer, 可选, 默认1): 页码。
    *   `pageSize` (Integer, 可选, 默认10): 每页数量。
    *   `status` (String, 可选): 状态筛选。可选值: `待确认`, `已完成`, `已取消`。
*   **成功响应 (200 OK)**:
    ```json
    {
      "code": 1,
      "msg": "success",
      "data": {
        "total": 2156,
        "items": [
          {
            "orderId": 501,
            "buyerId": 101,
            "sellerId": 102,
            "listingId": 1,
            "orderStatus": "待确认",
            "transactionPrice": 35.50,
            "orderTime": "2025-09-13T14:00:00",
            "completeTime": null
          }
        ]
      }
    }
    ```

---

### **模块六：数据查询模块 (Data Query Module - Text-to-SQL)**

本模块提供基于自然语言的智能数据查询功能，管理员可以使用日常语言查询数据库，系统会自动将其转换为SQL并返回结果。

#### **6.1 自然语言数据查询（推荐）**
*   **功能**: 使用自然语言查询平台数据，系统自动转换为SQL执行。
*   **Endpoint**: `POST /admin/data-query/query`
*   **认证**: **需要**
*   **请求体 (Request Body)**:
    ```json
    {
      "query": "查询本月注册的用户数量"  // String, 必填, 自然语言查询
    }
    ```
*   **成功响应 (200 OK)**:
    ```json
    {
      "code": 1,
      "msg": "success",
      "data": {
        "sql": "SELECT COUNT(*) AS count FROM user WHERE register_time >= DATE_FORMAT(NOW(), '%Y-%m-01')",
        "result": [
          { "count": 156 }
        ],
        "explanation": "本月共有156位新用户注册"
      }
    }
    ```
*   **响应字段说明**:
    *   `sql` (String): 系统生成的SQL语句
    *   `result` (Array): 查询结果数据
    *   `explanation` (String): 对查询结果的自然语言解释
*   **失败响应**:
    *   `400 Bad Request`: 查询内容为空。
    *   `500 Internal Server Error`: 查询执行失败。
*   **查询示例**:
    *   "查询今天新增的订单数量"
    *   "统计各分类的书籍数量"
    *   "查找信誉分最高的10个用户"
    *   "本周交易额是多少"
    *   "哪些书籍被浏览最多"

#### **6.2 自然语言SQL查询（备用）**
*   **功能**: 备用的Text-to-SQL接口。
*   **Endpoint**: `POST /admin/query/sql`
*   **认证**: **需要**
*   **请求体 (Request Body)**:
    ```json
    {
      "query": "统计每个分类下有多少本书"  // String, 必填
    }
    ```
*   **成功响应 (200 OK)**:
    ```json
    {
      "code": 1,
      "msg": "success",
      "data": {
        "sql": "SELECT c.category_name, COUNT(b.book_id) AS book_count FROM category c LEFT JOIN book b ON c.category_id = b.category_id GROUP BY c.category_id",
        "result": [
          { "category_name": "计算机", "book_count": 520 },
          { "category_name": "文学小说", "book_count": 380 },
          { "category_name": "外语学习", "book_count": 290 }
        ],
        "explanation": "各分类书籍数量统计如上"
      }
    }
    ```

#### **6.3 Text-to-SQL查询（备用）**
*   **功能**: 另一个备用的Text-to-SQL接口。
*   **Endpoint**: `POST /admin/text-to-sql/query`
*   **认证**: **需要**
*   **请求体 (Request Body)**:
    ```json
    {
      "query": "查询所有在售书籍的平均价格"  // String, 必填
    }
    ```
*   **成功响应 (200 OK)**:
    ```json
    {
      "code": 1,
      "msg": "success",
      "data": {
        "sql": "SELECT AVG(price) AS avg_price FROM listing WHERE status = '在售'",
        "result": [
          { "avg_price": 28.75 }
        ],
        "explanation": "在售书籍的平均价格为28.75元"
      }
    }
    ```

---

### **附录：管理员角色说明**

| 角色 | 标识 | 权限说明 |
|------|------|----------|
| 超级管理员 | `super_admin` | 拥有所有权限，可管理其他管理员 |
| 普通管理员 | `admin` | 可进行日常运营管理操作 |

---

### **附录：常用查询示例**

以下是Text-to-SQL功能支持的一些常用查询示例：

| 查询目的 | 自然语言示例 |
|----------|-------------|
| 用户统计 | "查询总用户数"、"今天新增了多少用户" |
| 订单统计 | "本月完成的订单有多少"、"统计每日订单数量" |
| 书籍统计 | "在售书籍有多少本"、"哪个分类的书最多" |
| 交易分析 | "本周交易总额是多少"、"平均每单交易金额" |
| 用户分析 | "信誉分低于80的用户有多少"、"最活跃的卖家是谁" |

