package com.tiancai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 数据库连接测试类
 * 用于验证数据库配置是否正确，以及触发器、存储过程是否已创建
 */
@SpringBootTest
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDatabaseConnection() {
        System.out.println("\n========================================");
        System.out.println("🔍 开始数据库连接测试");
        System.out.println("========================================\n");

        try (Connection conn = dataSource.getConnection()) {
            System.out.println("✅ 数据库连接成功！");
            System.out.println("   数据库URL: " + conn.getMetaData().getURL());
            System.out.println("   用户名: " + conn.getMetaData().getUserName());
            System.out.println("   数据库产品: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("   数据库版本: " + conn.getMetaData().getDatabaseProductVersion());
            System.out.println();

            // 检查表是否存在
            checkTables(conn);
            
            // 检查触发器是否存在
            checkTriggers(conn);
            
            // 检查存储过程是否存在
            checkProcedures(conn);
            
            // 统计数据
            countData(conn);

        } catch (Exception e) {
            System.out.println("❌ 数据库连接失败！");
            System.out.println("   错误信息: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n========================================");
        System.out.println("测试完成");
        System.out.println("========================================\n");
    }

    private void checkTables(Connection conn) throws Exception {
        System.out.println("📊 检查数据库表...");
        String[] tables = {"user", "category", "book", "book_category", "listing", "orders", "comment"};
        
        try (Statement stmt = conn.createStatement()) {
            for (String table : tables) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM information_schema.tables " +
                        "WHERE table_schema = DATABASE() AND table_name = '" + table + "'");
                if (rs.next() && rs.getInt("cnt") > 0) {
                    System.out.println("   ✅ 表 '" + table + "' 存在");
                } else {
                    System.out.println("   ❌ 表 '" + table + "' 不存在");
                }
            }
        }
        System.out.println();
    }

    private void checkTriggers(Connection conn) throws Exception {
        System.out.println("⚡ 检查触发器...");
        String[] triggers = {
            "after_order_insert",
            "after_order_update_complete",
            "after_order_update_cancel"
        };
        
        try (Statement stmt = conn.createStatement()) {
            for (String trigger : triggers) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM information_schema.triggers " +
                        "WHERE trigger_schema = DATABASE() AND trigger_name = '" + trigger + "'");
                if (rs.next() && rs.getInt("cnt") > 0) {
                    System.out.println("   ✅ 触发器 '" + trigger + "' 已创建");
                } else {
                    System.out.println("   ⚠️  触发器 '" + trigger + "' 未创建");
                }
            }
        }
        System.out.println();
    }

    private void checkProcedures(Connection conn) throws Exception {
        System.out.println("🔧 检查存储过程...");
        String[] procedures = {"complete_transaction", "register_user"};
        
        try (Statement stmt = conn.createStatement()) {
            for (String procedure : procedures) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM information_schema.routines " +
                        "WHERE routine_schema = DATABASE() AND routine_name = '" + procedure + "' AND routine_type = 'PROCEDURE'");
                if (rs.next() && rs.getInt("cnt") > 0) {
                    System.out.println("   ✅ 存储过程 '" + procedure + "' 已创建");
                } else {
                    System.out.println("   ⚠️  存储过程 '" + procedure + "' 未创建");
                }
            }
        }
        System.out.println();
    }

    private void countData(Connection conn) throws Exception {
        System.out.println("📈 数据统计...");
        String[] tables = {"user", "category", "book", "listing", "orders"};
        
        try (Statement stmt = conn.createStatement()) {
            for (String table : tables) {
                try {
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM " + table);
                    if (rs.next()) {
                        int count = rs.getInt("cnt");
                        System.out.println("   📊 表 '" + table + "' 有 " + count + " 条记录");
                    }
                } catch (Exception e) {
                    System.out.println("   ❌ 无法统计表 '" + table + "': " + e.getMessage());
                }
            }
        }
        System.out.println();
    }
}


