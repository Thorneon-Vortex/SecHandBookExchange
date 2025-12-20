-- 创建管理员表
CREATE TABLE IF NOT EXISTS admin (
    admin_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（MD5加密）',
    role ENUM('super_admin', 'admin', 'operator') DEFAULT 'admin' COMMENT '角色',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    last_login_time DATETIME COMMENT '最后登录时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 插入默认管理员账号
-- 用户名：admin
-- 密码：admin123
-- MD5值：0192023a7bbd73250516f069df18b500
INSERT INTO admin (username, password, role) 
VALUES ('admin', '0192023a7bbd73250516f069df18b500', 'super_admin')
ON DUPLICATE KEY UPDATE username=username;


