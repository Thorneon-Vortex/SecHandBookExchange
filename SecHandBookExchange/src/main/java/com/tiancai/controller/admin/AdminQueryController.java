package com.tiancai.controller.admin;

import com.tiancai.ai.AdminTextToSqlService;
import com.tiancai.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员 - 数据查询控制器（Text-to-SQL）
 */
@Slf4j
@RestController
@RequestMapping("/admin/query")
public class AdminQueryController {

    @Autowired
    private AdminTextToSqlService textToSqlService;

    /**
     * 自然语言查询数据
     */
    @PostMapping("/sql")
    public Result queryByNaturalLanguage(@RequestBody QueryRequest request) {
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            return Result.error("查询内容不能为空");
        }
        
        try {
            AdminTextToSqlService.QueryResult result = textToSqlService.queryByNaturalLanguage(request.getQuery());
            return Result.success(result);
        } catch (Exception e) {
            log.error("Text-to-SQL查询异常", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询请求
     */
    public static class QueryRequest {
        private String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }
}

