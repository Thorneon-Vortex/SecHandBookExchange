# 阿里云OSS配置说明

## 问题描述
运行Demo.java时出现OSS签名验证失败错误：
```
Error Message: The request signature we calculated does not match the signature you provided. Check your key and signing method.
Error Code: SignatureDoesNotMatch
```

## 解决方案

### 方案1：使用环境变量（推荐）

#### Windows PowerShell设置环境变量：
```powershell
# 临时设置（当前命令行窗口有效）
$env:OSS_ACCESS_KEY_ID="您的AccessKeyId"
$env:OSS_ACCESS_KEY_SECRET="您的AccessKeySecret"

# 验证设置
echo $env:OSS_ACCESS_KEY_ID
echo $env:OSS_ACCESS_KEY_SECRET
```

#### Windows系统永久设置：
1. 右键"此电脑" → "属性" → "高级系统设置" → "环境变量"
2. 在"用户变量"或"系统变量"中添加：
   - 变量名：`OSS_ACCESS_KEY_ID`，变量值：您的AccessKeyId
   - 变量名：`OSS_ACCESS_KEY_SECRET`，变量值：您的AccessKeySecret
3. 重启命令行窗口或IDE

### 方案2：修改代码直接配置（仅用于测试）

在 `Demo.java` 文件中找到以下代码：
```java
String accessKeyId = "LTAI5tLhWVJwUTwKMBdGp3ZG"; // 您的AccessKeyId
String accessKeySecret = "您的AccessKeySecret"; // 请替换为您的真实Secret
```

将 `"您的AccessKeySecret"` 替换为您的真实AccessKeySecret。

## 获取阿里云OSS AccessKey

1. 登录阿里云控制台
2. 进入"访问控制" → "用户" → "AccessKey管理"
3. 创建AccessKey或查看现有AccessKey
4. 记录AccessKeyId和AccessKeySecret

## 验证配置

运行Demo.java，如果看到以下输出说明配置成功：
```
尝试从环境变量获取OSS认证信息...
✓ 成功从环境变量获取认证信息
```

## 注意事项

1. **安全提醒**：不要将AccessKeySecret硬编码到代码中，生产环境请使用环境变量
2. **权限检查**：确保AccessKey具有OSS的读写权限
3. **网络检查**：确保网络可以访问阿里云OSS服务
4. **文件路径**：确保要上传的文件 `D:\code\piano.png` 存在

## 常见问题

### Q: 仍然提示签名错误
A: 检查AccessKeyId和AccessKeySecret是否正确，确保没有多余的空格或特殊字符

### Q: 环境变量设置后仍然无效
A: 重启IDE或命令行窗口，确保环境变量生效

### Q: 文件上传失败
A: 检查文件路径是否正确，文件是否存在且有读取权限

