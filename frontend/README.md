# 校园二手书交易平台 - 前端项目

一个现代化的、美观的二手书交易平台前端应用，基于 Vue 3 + Element Plus 构建。

## 🎨 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **开发语言**: JavaScript

## 📦 安装依赖

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install
```

## 🚀 启动开发服务器

```bash
npm run dev
```

默认运行在 `http://localhost:3000`

## 🏗️ 构建生产版本

```bash
npm run build
```

构建后的文件将输出到 `dist` 目录。

## 📋 功能模块

### 1. 用户模块
- ✅ 用户注册
- ✅ 用户登录
- ✅ 个人信息查看和编辑
- ✅ 信誉积分展示

### 2. 书籍模块
- ✅ 首页展示
- ✅ 书籍搜索和筛选
- ✅ 书籍列表展示（分页）
- ✅ 书籍详情查看
- ✅ 发布书籍（出售/赠送）
- ✅ 下架书籍

### 3. 订单模块
- ✅ 创建订单
- ✅ 查看订单列表（买家/卖家视角）
- ✅ 确认交易完成
- ✅ 取消订单

### 4. 分类模块
- ✅ 查看所有分类
- ✅ 按分类筛选书籍

## 📂 项目结构

```
frontend/
├── public/              # 静态资源
├── src/
│   ├── api/            # API接口封装
│   │   ├── user.js
│   │   ├── listing.js
│   │   ├── order.js
│   │   └── category.js
│   ├── assets/         # 资源文件
│   ├── layouts/        # 布局组件
│   │   └── MainLayout.vue
│   ├── router/         # 路由配置
│   │   └── index.js
│   ├── stores/         # 状态管理
│   │   └── user.js
│   ├── utils/          # 工具函数
│   │   └── request.js  # Axios封装
│   ├── views/          # 页面组件
│   │   ├── Home.vue           # 首页
│   │   ├── Login.vue          # 登录页
│   │   ├── Register.vue       # 注册页
│   │   ├── Listings.vue       # 书籍列表
│   │   ├── ListingDetail.vue  # 书籍详情
│   │   ├── Publish.vue        # 发布书籍
│   │   ├── Profile.vue        # 个人中心
│   │   └── Orders.vue         # 订单管理
│   ├── App.vue         # 根组件
│   └── main.js         # 入口文件
├── index.html          # HTML模板
├── vite.config.js      # Vite配置
└── package.json        # 项目配置
```

## 🔧 配置说明

### API代理配置

在 `vite.config.js` 中已配置代理：

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

所有 `/api` 开头的请求会被代理到后端服务器 `http://localhost:8080`。

### 路由守卫

已配置路由守卫，需要登录的页面会自动跳转到登录页面：

- `/publish` - 发布书籍
- `/profile` - 个人中心
- `/orders` - 订单管理

## 🎯 使用指南

### 1. 首次使用

1. 确保后端服务已启动（默认端口8080）
2. 安装前端依赖：`npm install`
3. 启动开发服务器：`npm run dev`
4. 访问 `http://localhost:3000`

### 2. 注册账号

1. 点击右上角"注册"按钮
2. 填写学号、昵称、密码、联系方式
3. 提交注册

### 3. 登录系统

1. 点击右上角"登录"按钮
2. 输入学号和密码
3. 登录成功后会自动跳转

### 4. 发布书籍

1. 登录后点击"发布书籍"
2. 填写书籍信息（ISBN、书名、作者等）
3. 选择发布类型（出售/赠送）
4. 设置价格和新旧程度
5. 提交发布

### 5. 购买书籍

1. 在首页或书籍列表浏览书籍
2. 点击书籍查看详情
3. 点击"立即购买"创建订单
4. 在订单页面查看联系方式
5. 线下完成交易后点击"确认完成"

## 🎨 界面特色

- 🎯 **现代化设计**: 采用渐变色背景、卡片式布局
- 📱 **响应式布局**: 完美适配PC和移动设备
- 🎭 **流畅动画**: 悬停效果、过渡动画
- 🌈 **Element Plus**: 使用Element Plus组件库，UI美观统一
- 🔍 **搜索筛选**: 支持关键词搜索、分类筛选、多种排序
- 💫 **用户体验**: 加载状态、空状态、错误提示完善

## 📝 注意事项

1. **开发环境**: 需要Node.js 16+
2. **后端依赖**: 前端需要后端API支持才能正常工作
3. **浏览器兼容**: 建议使用Chrome、Firefox、Edge等现代浏览器
4. **跨域问题**: 开发环境通过Vite代理解决，生产环境需配置CORS

## 🐛 常见问题

### Q: 启动失败，提示端口被占用？
A: 修改 `vite.config.js` 中的 `server.port` 配置。

### Q: 请求后端API失败？
A: 
1. 确保后端服务已启动（localhost:8080）
2. 检查 `vite.config.js` 中的代理配置
3. 查看浏览器控制台的错误信息

### Q: 登录后刷新页面需要重新登录？
A: Token已存储在localStorage中，不应该出现此问题。检查浏览器是否禁用了localStorage。

### Q: 图片无法显示？
A: 
1. 检查图片URL是否正确
2. 图片服务器是否允许跨域访问
3. 使用Element Plus的error插槽显示占位图

## 📄 License

MIT

## 👥 作者

校园二手书交易平台开发团队


