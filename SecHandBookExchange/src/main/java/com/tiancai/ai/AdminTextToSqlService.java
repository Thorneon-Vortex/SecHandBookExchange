package com.tiancai.ai;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * B端 Text-to-SQL 服务
 * 将自然语言查询转换为 SQL 并执行
 */
@Slf4j
@Service
public class AdminTextToSqlService {

    @Autowired
    private DeepSeekClient deepSeekClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SQL_SYSTEM_PROMPT = """
            你是一个SQL专家。根据管理员的自然语言请求，生成MySQL查询语句。
            
            数据库表结构如下：
            
            1. user 表：
               - user_id (INT, 主键, 自增)
               - student_id (VARCHAR, 学号, 唯一)
               - nickname (VARCHAR, 昵称)
               - credit_score (INT, 信誉分, 默认100)
               - contact_info (VARCHAR, 联系方式)
               - register_time (DATETIME, 注册时间)
            
            2. book 表：
               - book_id (INT, 主键, 自增)
               - isbn (VARCHAR)
               - title (VARCHAR, 书名)
               - author (VARCHAR, 作者)
               - publisher (VARCHAR, 出版社)
               - publication_year (INT, 出版年份)
               - cover_image_url (VARCHAR, 封面图片)
            
            3. listing 表：
               - listing_id (INT, 主键, 自增)
               - seller_id (INT, 外键→user.user_id)
               - book_id (INT, 外键→book.book_id)
               - price (DECIMAL, 价格)
               - condition_desc (VARCHAR, 书籍成色描述)
               - status (ENUM: '在售','已预定','已售出','已下架')
               - listing_type (ENUM: '出售','赠送')
               - post_time (DATETIME, 发布时间)
               - description (TEXT, 描述)
            
            4. orders 表：
               - order_id (INT, 主键, 自增)
               - listing_id (INT, 外键→listing.listing_id)
               - buyer_id (INT, 外键→user.user_id)
               - order_status (ENUM: '待确认','已完成','已取消')
               - transaction_price (DECIMAL)
               - order_time (DATETIME, 下单时间)
            
            5. category 表：
               - category_id (INT, 主键)
               - category_name (VARCHAR, 分类名称)
            
            6. book_category 表（多对多关联）：
               - book_id (INT, 外键→book.book_id)
               - category_id (INT, 外键→category.category_id)
            
            7. admin 表：
               - admin_id (INT, 主键)
               - username (VARCHAR, 用户名)
               - role (ENUM: 'super_admin', 'admin', 'operator')
               - created_time (DATETIME, 创建时间)
               - last_login_time (DATETIME, 最后登录时间)
            
            规则：
            1. 只生成SELECT查询语句，绝对不允许INSERT/UPDATE/DELETE/DROP等危险操作
            2. 必须添加 LIMIT 100 限制返回结果（防止返回过多数据）
            3. 只返回纯SQL语句，不要有任何解释或markdown格式
            4. 如果无法生成有效SQL或请求不适合用SQL处理，只返回"INVALID"
            5. 支持聚合函数（COUNT, SUM, AVG, MAX, MIN）
            6. 支持日期函数（DATE, YEAR, MONTH, DAY）
            7. 支持GROUP BY和ORDER BY
            
            示例请求和SQL：
            - "本月新增用户数" → SELECT COUNT(*) as count FROM user WHERE YEAR(register_time) = YEAR(NOW()) AND MONTH(register_time) = MONTH(NOW())
            - "信誉分最高的10个用户" → SELECT user_id, nickname, credit_score FROM user ORDER BY credit_score DESC LIMIT 10
            - "各状态的书籍数量" → SELECT status, COUNT(*) as count FROM listing GROUP BY status
            - "今日订单总金额" → SELECT SUM(transaction_price) as total FROM orders WHERE DATE(order_time) = CURDATE() AND order_status = '已完成'
            - "最受欢迎的书籍分类" → SELECT c.category_name, COUNT(bc.book_id) as book_count FROM category c LEFT JOIN book_category bc ON c.category_id = bc.category_id GROUP BY c.category_id ORDER BY book_count DESC LIMIT 10
            """;

    /**
     * 自然语言转SQL并执行查询
     */
    public QueryResult queryByNaturalLanguage(String userQuery) {
        // 1. 生成SQL
        String sql = generateSql(userQuery);
        log.info("生成的SQL: {}", sql);

        if ("INVALID".equalsIgnoreCase(sql.trim())) {
            return new QueryResult(false, "这个问题不太适合用数据查询来回答，请换个方式提问", null, null);
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
        
        // 清理响应（去除可能的markdown格式）
        String sql = response.trim();
        if (sql.startsWith("```")) {
            sql = sql.replaceAll("```sql\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();
        }
        
        return sql;
    }

    /**
     * 安全检查
     */
    private boolean isSafeQuery(String sql) {
        String upperSql = sql.toUpperCase().trim();
        
        // 必须是SELECT语句
        if (!upperSql.startsWith("SELECT")) {
            return false;
        }
        
        // 禁止危险操作
        String[] forbidden = {"DROP", "DELETE", "UPDATE", "INSERT", "TRUNCATE", "ALTER", 
                              "CREATE", "GRANT", "REVOKE", "EXEC", "EXECUTE", 
                              "INTO OUTFILE", "INTO DUMPFILE", "LOAD_FILE"};
        
        for (String kw : forbidden) {
            if (upperSql.contains(kw)) {
                return false;
            }
        }
        
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
        return String.format("查询完成，共找到 %d 条结果 📊", results.size());
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
