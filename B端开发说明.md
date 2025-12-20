# B端管理后台开发说明

## 一、项目结构

### 后端（共享）

```
SecHandBookExchange/src/main/java/com/tiancai/
├── entity/
│   └── Admin.java                    # 管理员实体
├── dto/
│   └── AdminLoginDTO.java           # 管理员登录DTO
├── mapper/
│   └── AdminMapper.java              # 管理员Mapper
├── service/
│   ├── AdminService.java             # 管理员服务接口
│   └── impl/
│       └── AdminServiceImpl.java     # 管理员服务实现
├── controller/
│   └── admin/                        # B端控制器
│       ├── AdminAuthController.java   # 认证
│       ├── AdminUserController.java   # 用户管理
│       ├── AdminListingController.java # 书籍管理
│       ├── AdminOrderController.java  # 订单管理
│       └── AdminDashboardController.java # 数据统计
└── interceptor/
    └── AdminInterceptor.java         # 管理员权限拦截器
```

### 前端（独立项目）

```
admin-frontend/
├── src/
│   ├── api/
│   │   └── admin.js                  # API封装
│   ├── views/
│   │   ├── Login.vue                  # 登录页
│   │   ├── Dashboard.vue              # 数据概览
│   │   ├── UserManagement.vue         # 用户管理
│   │   ├── ListingManagement.vue     # 书籍管理
│   │   └── OrderManagement.vue       # 订单管理
│   ├── layouts/
│   │   └── MainLayout.vue            # 主布局
│   ├── router/
│   │   └── index.js                  # 路由配置
│   ├── stores/
│   │   └── admin.js                  # Pinia状态管理
│   └── utils/
│       └── request.js                # Axios封装
```

---

## 二、数据库初始化

需要创建管理员表：

```sql
CREATE TABLE admin (
    admin_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('super_admin', 'admin', 'operator') DEFAULT 'admin',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_login_time DATETIME
);

-- 插入默认管理员（密码：admin123，MD5加密后）
INSERT INTO admin (username, password, role) 
VALUES ('admin', '0192023a7bbd73250516f069df18b500', 'super_admin');
```

> 注意：`0192023a7bbd73250516f069df18b500` 是 `admin123` 的 MD5 值

---

## 三、API 接口

### 1. 管理员登录

**POST** `/admin/auth/login`

请求体：
```json
{
  "username": "admin",
  "password": "admin123"
}
```

响应：
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "adminId": 1,
    "username": "admin",
    "role": "super_admin"
  }
}
```

### 2. 数据统计

**GET** `/admin/dashboard/statistics`

响应：
```json
{
  "code": 1,
  "data": {
    "userCount": 100,
    "listingCount": 500,
    "onSaleCount": 300,
    "orderCount": 200,
    "completedOrderCount": 150
  }
}
```

### 3. 用户管理

- **GET** `/admin/users?page=1&pageSize=10&keyword=xxx` - 获取用户列表
- **PUT** `/admin/users/{userId}/status?enabled=false` - 更新用户状态

### 4. 书籍管理

- **GET** `/admin/listings?page=1&pageSize=10&keyword=xxx&status=在售` - 获取书籍列表
- **PUT** `/admin/listings/{listingId}/take-down?reason=xxx` - 下架书籍

### 5. 订单管理

- **GET** `/admin/orders?page=1&pageSize=10&status=已完成` - 获取订单列表

### 6. Text-to-SQL 查询

- **POST** `/admin/text-to-sql/query`

请求体：
```json
{
  "query": "今天注册了多少用户"
}
```

响应：
```json
{
  "code": 1,
  "data": {
    "success": true,
    "message": "查询结果：5 📊",
    "data": [
      { "count": 5 }
    ],
    "sql": "SELECT COUNT(*) as count FROM user WHERE DATE(register_time) = CURDATE()"
  }
}
```

---

## 四、权限控制

### 后端拦截器

所有 `/admin/**` 接口（除 `/admin/auth/login`）都需要：
1. 携带 `Authorization: Bearer <token>` 请求头
2. Token 中 `type` 字段必须为 `"admin"`

### 前端路由守卫

- 访问需要认证的路由时，自动检查登录状态
- 未登录自动跳转到登录页
- 已登录访问登录页自动跳转到首页

---

## 五、启动步骤

### 1. 后端

```bash
cd SecHandBookExchange
mvn clean compile spring-boot:run
```

### 2. 前端

```bash
cd admin-frontend
npm install
npm run dev
```

访问：`http://localhost:3001`

### 3. 登录

- 用户名：`admin`
- 密码：`admin123`

---

## 六、功能说明

### 数据概览
- 显示用户总数、书籍总数、在售书籍数、已完成订单数
- **Text-to-SQL 智能查询**：支持用自然语言查询数据
  - 示例："今天注册了多少用户"、"最贵的书是什么"、"信誉分最高的10个用户"
  - 自动生成SQL并执行，显示查询结果和生成的SQL语句
  - 支持复杂查询：统计、排序、分组等

### 用户管理
- 查看用户列表（分页、搜索）
- 禁用/启用用户（需要User表添加status字段）

### 书籍管理
- 查看书籍列表（分页、搜索、状态筛选）
- 下架书籍（更新状态为"已下架"）

### 订单管理
- 查看订单列表（分页、状态筛选）

---

## 七、注意事项

1. **密码加密**：当前使用 MD5，生产环境建议使用 BCrypt
2. **用户状态**：User 表需要添加 `status` 字段才能实现禁用功能
3. **权限分级**：当前只有基础权限控制，可根据 `role` 字段扩展
4. **数据统计**：部分统计功能（如今日新增）需要扩展 Mapper 方法

---

## 八、Text-to-SQL 功能说明

### 功能特点

✅ **自然语言查询**：管理员可以用中文描述查询需求  
✅ **自动生成SQL**：AI自动将自然语言转换为SQL语句  
✅ **安全防护**：只允许SELECT查询，禁止危险操作  
✅ **结果展示**：以表格形式展示查询结果，并显示生成的SQL  

### 使用示例

| 自然语言查询 | 生成的SQL | 说明 |
|------------|---------|------|
| "今天注册了多少用户" | `SELECT COUNT(*) FROM user WHERE DATE(register_time) = CURDATE()` | 统计查询 |
| "最贵的书是什么" | `SELECT b.title, l.price FROM listing l JOIN book b ... ORDER BY l.price DESC LIMIT 100` | 排序查询 |
| "信誉分最高的10个用户" | `SELECT user_id, nickname, credit_score FROM user ORDER BY credit_score DESC LIMIT 10` | Top N查询 |
| "最近一周的订单数量" | `SELECT COUNT(*) FROM orders WHERE order_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)` | 时间范围查询 |
| "每个分类有多少本书" | `SELECT c.category_name, COUNT(bc.book_id) FROM category c LEFT JOIN book_category bc ... GROUP BY ...` | 分组统计 |

### 安全机制

- ✅ 只允许 `SELECT` 查询
- ✅ 禁止 `INSERT`、`UPDATE`、`DELETE`、`DROP` 等危险操作
- ✅ 自动添加 `LIMIT 100` 限制结果数量
- ✅ 不查询敏感字段（如password）

---

## 九、扩展建议

1. **角色权限**：根据 `role` 字段实现不同权限级别
2. **操作日志**：记录管理员的操作历史
3. **数据导出**：支持导出用户、订单等数据为 Excel
4. **内容审核**：书籍发布前需要审核
5. **数据可视化**：使用 ECharts 展示数据趋势
6. **查询历史**：保存常用查询，支持快速复用

