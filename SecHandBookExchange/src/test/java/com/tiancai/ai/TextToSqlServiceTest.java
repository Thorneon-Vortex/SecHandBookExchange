package com.tiancai.ai;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text-to-SQL 提取逻辑测试
 * 运行方式: mvn test -Dtest=TextToSqlServiceTest
 */
public class TextToSqlServiceTest {

    public static void main(String[] args) {
        System.out.println("========== Text-to-SQL 提取逻辑测试 ==========\n");
        
        // 测试用例
        String[] testCases = {
            // 1. 纯SQL
            "SELECT b.title, l.price FROM listing l JOIN book b ON l.book_id = b.book_id ORDER BY l.price DESC LIMIT 100",
            
            // 2. 带markdown代码块
            "```sql\nSELECT COUNT(*) FROM user LIMIT 100\n```",
            
            // 3. 带解释的响应
            "以下是查询最贵书籍的SQL：\nSELECT b.title, l.price FROM listing l JOIN book b ON l.book_id = b.book_id ORDER BY l.price DESC LIMIT 100",
            
            // 4. 多行SQL
            "SELECT b.title, l.price \nFROM listing l \nJOIN book b ON l.book_id = b.book_id \nORDER BY l.price DESC \nLIMIT 100",
            
            // 5. 带分号
            "SELECT COUNT(*) FROM user;",
            
            // 6. markdown + 解释
            "根据您的问题，这是SQL：\n```sql\nSELECT b.title, MAX(l.price) as max_price FROM listing l JOIN book b ON l.book_id = b.book_id GROUP BY b.title ORDER BY max_price DESC LIMIT 10\n```\n这个查询会返回最贵的书籍。",
            
            // 7. 无效响应
            "抱歉，我无法理解您的问题。",
            
            // 8. INVALID 响应
            "INVALID"
        };
        
        int passed = 0;
        int failed = 0;
        
        for (int i = 0; i < testCases.length; i++) {
            System.out.println("--- 测试用例 " + (i + 1) + " ---");
            System.out.println("输入: " + testCases[i].replace("\n", "\\n").substring(0, Math.min(80, testCases[i].length())) + "...");
            
            String result = extractSql(testCases[i]);
            System.out.println("输出: " + result);
            
            boolean isValid = result.toUpperCase().startsWith("SELECT") || result.equals("INVALID");
            if (isValid && i < 6) {
                // 前6个应该能提取出SELECT
                if (result.toUpperCase().startsWith("SELECT")) {
                    System.out.println("结果: ✅ 通过\n");
                    passed++;
                } else {
                    System.out.println("结果: ❌ 失败 - 应该能提取出SELECT语句\n");
                    failed++;
                }
            } else if (i >= 6) {
                // 后2个应该返回INVALID
                if (result.equals("INVALID")) {
                    System.out.println("结果: ✅ 通过 (正确返回INVALID)\n");
                    passed++;
                } else {
                    System.out.println("结果: ❌ 失败 - 应该返回INVALID\n");
                    failed++;
                }
            }
        }
        
        System.out.println("========== 测试结果 ==========");
        System.out.println("通过: " + passed + ", 失败: " + failed);
        
        if (failed > 0) {
            System.exit(1);
        }
    }
    
    /**
     * 模拟 extractSql 方法
     */
    private static String extractSql(String response) {
        if (response == null || response.isEmpty()) {
            return "INVALID";
        }
        
        String text = response.trim();
        
        // 1. 如果有markdown代码块，提取里面的内容
        if (text.contains("```")) {
            Matcher matcher = Pattern.compile("```(?:sql)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE).matcher(text);
            if (matcher.find()) {
                text = matcher.group(1).trim();
            }
        }
        
        // 2. 尝试找到SELECT语句（使用正则匹配整个SQL）
        Matcher selectMatcher = Pattern.compile(
            "(SELECT\\s+[\\s\\S]*?(?:LIMIT\\s+\\d+|;|$))", 
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        
        if (selectMatcher.find()) {
            text = selectMatcher.group(1).trim();
        }
        
        // 3. 去除末尾分号
        if (text.endsWith(";")) {
            text = text.substring(0, text.length() - 1).trim();
        }
        
        // 4. 最终检查：必须以SELECT开头
        if (!text.toUpperCase().startsWith("SELECT")) {
            return "INVALID";
        }
        
        return text;
    }
}

