package com.tiancai.ai;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Text-to-SQL 服务（B端专用）
 * 将自然语言查询转换为 SQL 并执行
 */
@Slf4j
@Service
public class TextToSqlService {

    @Autowired
    private DeepSeekClient deepSeekClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SQL_SYSTEM_PROMPT = """
            你是一个SQL专家。根据管理员的自然语言请求，生成MySQL查询语句。
            
            ⚠️ 重要提示：你必须严格按照下面的表结构生成SQL，不要使用表中不存在的字段！
            
            ═══════════════════════════════════════════════════════════════
            数据库表结构（请严格遵守，只使用列出的字段）：
            ═══════════════════════════════════════════════════════════════
            
            【user 表】用户信息
            ┌─────────────────┬─────────────┬──────────────────────────────┐
            │ 字段名           │ 类型         │ 说明                          │
            ├─────────────────┼─────────────┼──────────────────────────────┤
            │ user_id         │ INT         │ 主键, 自增                    │
            │ student_id      │ VARCHAR(20) │ 学号, 唯一                    │
            │ nickname        │ VARCHAR(50) │ 昵称                          │
            │ password        │ VARCHAR(255)│ 密码(禁止查询!)               │
            │ register_time   │ DATETIME    │ 注册时间                      │
            │ contact_info    │ VARCHAR(100)│ 联系方式                      │
            │ credit_score    │ INT         │ 信誉积分, 默认100             │
            └─────────────────┴─────────────┴──────────────────────────────┘
            
            【book 表】书籍基本信息
            ⚠️ 注意：此表没有时间字段！书籍发布时间在listing表的post_time字段！
            ┌─────────────────┬─────────────┬──────────────────────────────┐
            │ 字段名           │ 类型         │ 说明                          │
            ├─────────────────┼─────────────┼──────────────────────────────┤
            │ book_id         │ INT         │ 主键, 自增                    │
            │ isbn            │ VARCHAR(20) │ ISBN号, 唯一                  │
            │ title           │ VARCHAR(255)│ 书名                          │
            │ author          │ VARCHAR(100)│ 作者                          │
            │ publisher       │ VARCHAR(100)│ 出版社                        │
            │ publication_year│ VARCHAR(10) │ 出版年份                      │
            │ cover_image_url │ VARCHAR(512)│ 封面图片URL                   │
            └─────────────────┴─────────────┴──────────────────────────────┘
            
            【listing 表】书籍发布/上架信息
            ┌─────────────────┬─────────────┬──────────────────────────────┐
            │ 字段名           │ 类型         │ 说明                          │
            ├─────────────────┼─────────────┼──────────────────────────────┤
            │ listing_id      │ INT         │ 主键, 自增                    │
            │ seller_id       │ INT         │ 发布者ID, 外键→user.user_id  │
            │ book_id         │ INT         │ 书籍ID, 外键→book.book_id    │
            │ price           │ DECIMAL(10,2)│ 价格                         │
            │ condition_desc  │ VARCHAR(20) │ 新旧程度描述                  │
            │ listing_type    │ ENUM        │ '出售' 或 '赠送'              │
            │ status          │ ENUM        │ '在售','已预定','已售出','已下架'│
            │ post_time       │ DATETIME    │ 发布时间 ← 查上架时间用这个!  │
            │ description     │ TEXT        │ 详细描述                      │
            └─────────────────┴─────────────┴──────────────────────────────┘
            
            【orders 表】订单信息
            ┌─────────────────┬─────────────┬──────────────────────────────┐
            │ 字段名           │ 类型         │ 说明                          │
            ├─────────────────┼─────────────┼──────────────────────────────┤
            │ order_id        │ INT         │ 主键, 自增                    │
            │ listing_id      │ INT         │ 发布ID, 外键→listing         │
            │ buyer_id        │ INT         │ 购买者ID, 外键→user          │
            │ order_time      │ DATETIME    │ 下单时间                      │
            │ order_status    │ ENUM        │ '待确认','已完成','已取消'    │
            │ transaction_price│ DECIMAL(10,2)│ 交易价格                    │
            └─────────────────┴─────────────┴──────────────────────────────┘
            
            【category 表】书籍分类
            ┌─────────────────┬─────────────┬──────────────────────────────┐
            │ 字段名           │ 类型         │ 说明                          │
            ├─────────────────┼─────────────┼──────────────────────────────┤
            │ category_id     │ INT         │ 主键, 自增                    │
            │ category_name   │ VARCHAR(50) │ 分类名称                      │
            └─────────────────┴─────────────┴──────────────────────────────┘
            
            【book_category 表】书籍与分类的关联表（多对多）
            ┌─────────────────┬─────────────┬──────────────────────────────┐
            │ 字段名           │ 类型         │ 说明                          │
            ├─────────────────┼─────────────┼──────────────────────────────┤
            │ book_id         │ INT         │ 外键→book.book_id            │
            │ category_id     │ INT         │ 外键→category.category_id    │
            └─────────────────┴─────────────┴──────────────────────────────┘
            
            【comment 表】评论
            ┌─────────────────┬─────────────┬──────────────────────────────┐
            │ 字段名           │ 类型         │ 说明                          │
            ├─────────────────┼─────────────┼──────────────────────────────┤
            │ comment_id      │ INT         │ 主键, 自增                    │
            │ listing_id      │ INT         │ 发布ID, 外键→listing         │
            │ user_id         │ INT         │ 评论者ID, 外键→user          │
            │ content         │ TEXT        │ 评论内容                      │
            │ comment_time    │ DATETIME    │ 评论时间                      │
            └─────────────────┴─────────────┴──────────────────────────────┘
            
            【admin 表】管理员
            ┌─────────────────┬─────────────┬──────────────────────────────┐
            │ 字段名           │ 类型         │ 说明                          │
            ├─────────────────┼─────────────┼──────────────────────────────┤
            │ admin_id        │ INT         │ 主键                          │
            │ username        │ VARCHAR     │ 用户名                        │
            │ password        │ VARCHAR     │ 密码(禁止查询!)               │
            │ role            │ ENUM        │ 'super_admin','admin','operator'│
            │ created_time    │ DATETIME    │ 创建时间                      │
            │ last_login_time │ DATETIME    │ 最后登录时间                  │
            └─────────────────┴─────────────┴──────────────────────────────┘
            
            ═══════════════════════════════════════════════════════════════
            规则（必须遵守）：
            ═══════════════════════════════════════════════════════════════
            1. 只生成SELECT查询语句，绝对不允许INSERT/UPDATE/DELETE/DROP等危险操作
            2. 必须添加 LIMIT 100 限制返回结果（防止数据量过大）
            3. 只返回纯SQL语句，不要有任何解释或markdown格式
            4. 如果无法生成有效SQL或请求不适合用SQL处理，只返回"INVALID"
            5. 不要查询password字段
            6. ⚠️ 只使用上面表结构中列出的字段，不要臆造不存在的字段！
            
            ═══════════════════════════════════════════════════════════════
            示例请求和SQL（请仔细学习）：
            ═══════════════════════════════════════════════════════════════
            
            用户相关查询（user表有register_time）：
            - "今天注册了多少用户" → SELECT COUNT(*) as count FROM user WHERE DATE(register_time) = CURDATE() LIMIT 100
            - "信誉分最高的10个用户" → SELECT user_id, nickname, credit_score FROM user ORDER BY credit_score DESC LIMIT 10
            - "信誉分低于80的用户" → SELECT user_id, nickname, credit_score FROM user WHERE credit_score < 80 LIMIT 100
            
            书籍相关查询（book表没有时间字段，上架时间在listing.post_time）：
            - "今天上架了多少书" → SELECT COUNT(*) as count FROM listing WHERE DATE(post_time) = CURDATE() LIMIT 100
            - "最近一周发布的书籍" → SELECT b.title, b.author, l.price, l.post_time FROM listing l JOIN book b ON l.book_id = b.book_id WHERE l.post_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) ORDER BY l.post_time DESC LIMIT 100
            - "最贵的书是什么" → SELECT b.title, l.price FROM listing l JOIN book b ON l.book_id = b.book_id WHERE l.status = '在售' ORDER BY l.price DESC LIMIT 100
            - "有多少本在售的书" → SELECT COUNT(*) as count FROM listing WHERE status = '在售' LIMIT 100
            - "人民邮电出版社的书有哪些" → SELECT title, author FROM book WHERE publisher = '人民邮电出版社' LIMIT 100
            
            订单相关查询：
            - "最近一周的订单数量" → SELECT COUNT(*) as count FROM orders WHERE order_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) LIMIT 100
            - "已完成的订单总金额" → SELECT SUM(transaction_price) as total FROM orders WHERE order_status = '已完成' LIMIT 100
            - "今天的订单" → SELECT order_id, listing_id, buyer_id, order_status, transaction_price FROM orders WHERE DATE(order_time) = CURDATE() LIMIT 100
            
            分类相关查询：
            - "每个分类有多少本书" → SELECT c.category_name, COUNT(bc.book_id) as count FROM category c LEFT JOIN book_category bc ON c.category_id = bc.category_id GROUP BY c.category_id, c.category_name LIMIT 100
            - "计算机类的书籍" → SELECT b.title, b.author FROM book b JOIN book_category bc ON b.book_id = bc.book_id JOIN category c ON bc.category_id = c.category_id WHERE c.category_name = '计算机' LIMIT 100
            """;

    /**
     * 自然语言转SQL并执行查询
     */
    public QueryResult queryByNaturalLanguage(String userQuery) {
        // 1. 生成SQL
        String sql = generateSql(userQuery);
        log.info("生成的SQL: {}", sql);

        if ("INVALID".equalsIgnoreCase(sql.trim())) {
            return new QueryResult(false, "无法理解您的问题，请尝试更具体的描述，例如：'有多少用户'、'最贵的书是什么'", null, null);
        }
        
        if ("API_ERROR".equalsIgnoreCase(sql.trim())) {
            return new QueryResult(false, "AI服务暂时不可用，请检查网络连接或稍后再试", null, null);
        }

        // 2. 安全检查
        if (!isSafeQuery(sql)) {
            log.warn("不安全的SQL被拒绝: {}", sql);
            return new QueryResult(false, "查询请求不安全，已被拒绝", null, null);
        }

        try {
            // 3. 执行查询
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            // 4. 生成友好的回复
            String friendlyAnswer = generateFriendlyAnswer(userQuery, results);
            
            return new QueryResult(true, friendlyAnswer, results, sql);
        } catch (Exception e) {
            log.error("SQL执行失败: {}", sql, e);
            return new QueryResult(false, "查询执行失败: " + e.getMessage(), null, null);
        }
    }

    /**
     * 生成SQL
     */
    private String generateSql(String userQuery) {
        String response = deepSeekClient.chat(SQL_SYSTEM_PROMPT, userQuery);
        log.info("大模型原始响应: [{}]", response);
        
        // 检查是否是API错误响应
        if (response == null || response.isEmpty()) {
            log.error("大模型返回空响应");
            return "API_ERROR";
        }
        if (response.startsWith("抱歉") || response.contains("服务") && response.contains("不可用")) {
            log.error("大模型服务异常: {}", response);
            return "API_ERROR";
        }
        
        // 清理响应，提取纯SQL
        String sql = extractSql(response);
        log.info("提取后的SQL: [{}]", sql);
        
        return sql;
    }
    
    /**
     * 从大模型响应中提取纯SQL - 简化版本
     */
    private String extractSql(String response) {
        if (response == null || response.isEmpty()) {
            return "INVALID";
        }
        
        String text = response.trim();
        log.debug("开始提取SQL，原始文本长度: {}", text.length());
        
        // 1. 如果有markdown代码块，提取里面的内容
        if (text.contains("```")) {
            java.util.regex.Matcher matcher = Pattern.compile("```(?:sql)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE).matcher(text);
            if (matcher.find()) {
                text = matcher.group(1).trim();
                log.debug("从markdown代码块提取: {}", text);
            }
        }
        
        // 2. 尝试找到SELECT语句（使用正则匹配整个SQL）
        java.util.regex.Matcher selectMatcher = Pattern.compile(
            "(SELECT\\s+[\\s\\S]*?(?:LIMIT\\s+\\d+|;|$))", 
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        
        if (selectMatcher.find()) {
            text = selectMatcher.group(1).trim();
            log.debug("正则匹配到SELECT语句: {}", text);
        }
        
        // 3. 去除末尾分号
        if (text.endsWith(";")) {
            text = text.substring(0, text.length() - 1).trim();
        }
        
        // 4. 最终检查：必须以SELECT开头
        if (!text.toUpperCase().startsWith("SELECT")) {
            log.warn("无法提取有效SQL，处理后文本: {}", text);
            log.warn("原始响应: {}", response);
            return "INVALID";
        }
        
        return text;
    }

    /**
     * 安全检查 - 简单高效，只检查真正危险的操作
     */
    private boolean isSafeQuery(String sql) {
        if (sql == null || sql.isEmpty()) {
            return false;
        }
        
        String upperSql = sql.toUpperCase().trim();
        
        // 1. 必须以SELECT开头
        if (!upperSql.startsWith("SELECT")) {
            log.warn("SQL不是SELECT语句: {}", sql);
            return false;
        }
        
        // 2. 禁止子查询中的危险操作（检查整个SQL）
        // 使用简单的关键字检测，但只检测真正危险的DML/DDL语句
        String[] dangerousStatements = {
            "DROP TABLE", "DROP DATABASE", "DROP INDEX",
            "DELETE FROM", "DELETE WHERE",
            "UPDATE SET", "UPDATE WHERE", 
            "INSERT INTO", "INSERT VALUES",
            "TRUNCATE TABLE",
            "ALTER TABLE", "ALTER DATABASE",
            "CREATE TABLE", "CREATE DATABASE",
            "GRANT ", "REVOKE ",
            "INTO OUTFILE", "INTO DUMPFILE", "LOAD_FILE", "LOAD DATA"
        };
        
        for (String dangerous : dangerousStatements) {
            if (upperSql.contains(dangerous)) {
                log.warn("SQL包含危险语句 '{}': {}", dangerous, sql);
                return false;
            }
        }
        
        // 3. 禁止查询password字段（简单检测：SELECT ... password ... FROM）
        // 只在SELECT和FROM之间检查password
        int selectPos = upperSql.indexOf("SELECT");
        int fromPos = upperSql.indexOf("FROM");
        if (selectPos >= 0 && fromPos > selectPos) {
            String selectClause = upperSql.substring(selectPos, fromPos);
            // 检查是否包含password关键字（作为列名，不是字符串）
            if (selectClause.matches(".*\\bPASSWORD\\b.*") && !selectClause.contains("'PASSWORD'") && !selectClause.contains("\"PASSWORD\"")) {
                log.warn("SQL尝试查询password字段: {}", sql);
                return false;
            }
        }
        
        log.info("SQL安全检查通过: {}", sql);
        return true;
    }

    /**
     * 生成友好的回复
     */
    private String generateFriendlyAnswer(String userQuery, List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            return "查询完成，但没有找到相关数据 📭";
        }
        
        // 如果只有一行一列（如COUNT查询）
        if (results.size() == 1 && results.get(0).size() == 1) {
            Object value = results.get(0).values().iterator().next();
            return String.format("查询结果：%s 📊", value);
        }
        
        // 多行结果
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("共找到 %d 条结果：\n", Math.min(results.size(), 100)));
        
        int count = 0;
        for (Map<String, Object> row : results) {
            if (count >= 10) {
                sb.append("...(更多结果请查看表格)");
                break;
            }
            sb.append("• ");
            for (Object value : row.values()) {
                sb.append(value).append(" ");
            }
            sb.append("\n");
            count++;
        }
        
        return sb.toString().trim();
    }

    /**
     * 查询结果
     */
    @Data
    public static class QueryResult {
        private boolean success;
        private String message;
        private List<Map<String, Object>> data;
        private String sql;

        public QueryResult(boolean success, String message, List<Map<String, Object>> data, String sql) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.sql = sql;
        }
    }
}


