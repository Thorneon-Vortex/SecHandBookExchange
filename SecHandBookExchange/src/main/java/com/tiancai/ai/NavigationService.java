package com.tiancai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 智能导航服务
 * 识别用户意图并推荐页面跳转
 */
@Slf4j
@Service
public class NavigationService {

    @Autowired
    private DeepSeekClient deepSeekClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String NAVIGATION_PROMPT = """
            你是一个智能导航助手。根据用户的输入，识别用户的意图并推荐相应的页面。
            
            可用的页面和功能：
            1. home - 首页，浏览最新书籍和热门分类
            2. listings - 书籍市场，搜索和浏览所有在售书籍
            3. publish - 发布书籍，出售或赠送自己的书籍（需要登录）
            4. orders - 我的订单，查看购买和出售的订单（需要登录）
            5. profile - 个人中心，查看和修改个人信息（需要登录）
            6. login - 登录页面
            7. register - 注册页面
            
            请分析用户意图，返回严格的JSON格式（不要有任何其他文字）：
            {"intent":"用户意图简述","page":"页面标识","confidence":0.9,"params":{},"suggestion":"给用户的建议文字"}
            
            其中params是可选的查询参数，例如：
            - 搜索书籍时：{"keyword": "数据库"}
            - 筛选分类时：{"categoryId": 1}
            
            示例：
            - 用户说"我想卖书" → {"intent":"发布书籍","page":"publish","confidence":0.95,"params":{},"suggestion":"好的，我帮您跳转到发布书籍页面 📚"}
            - 用户说"有没有数据库的书" → {"intent":"搜索书籍","page":"listings","confidence":0.9,"params":{"keyword":"数据库"},"suggestion":"帮您搜索数据库相关的书籍 🔍"}
            - 用户说"查看我买的书" → {"intent":"查看订单","page":"orders","confidence":0.95,"params":{},"suggestion":"为您跳转到订单页面 📋"}
            - 用户说"注册账号" → {"intent":"注册","page":"register","confidence":0.95,"params":{},"suggestion":"好的，为您跳转到注册页面 ✨"}
            
            只返回JSON，不要有markdown格式或其他文字。
            """;

    /**
     * 分析用户意图并返回导航建议
     */
    public NavigationResult analyzeAndNavigate(String userInput) {
        String response = deepSeekClient.chat(NAVIGATION_PROMPT, userInput);
        log.info("导航分析响应: {}", response);

        try {
            // 清理可能的markdown格式
            String jsonStr = response.trim();
            if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.replaceAll("```json\\s*", "")
                        .replaceAll("```\\s*", "")
                        .trim();
            }
            
            JsonNode json = objectMapper.readTree(jsonStr);
            
            String intent = json.path("intent").asText("未知意图");
            String page = json.path("page").asText("home");
            double confidence = json.path("confidence").asDouble(0.5);
            String suggestion = json.path("suggestion").asText("已为您跳转");
            
            // 解析params
            Map<String, Object> params = new HashMap<>();
            JsonNode paramsNode = json.path("params");
            if (paramsNode.isObject()) {
                paramsNode.fields().forEachRemaining(entry -> {
                    JsonNode value = entry.getValue();
                    if (value.isTextual()) {
                        params.put(entry.getKey(), value.asText());
                    } else if (value.isNumber()) {
                        params.put(entry.getKey(), value.asInt());
                    }
                });
            }
            
            return new NavigationResult(intent, page, confidence, params, suggestion);
            
        } catch (Exception e) {
            log.error("解析导航响应失败", e);
            return new NavigationResult(
                    "unknown",
                    "home",
                    0.5,
                    new HashMap<>(),
                    "抱歉，我不太理解您的意思，已为您跳转到首页 🏠"
            );
        }
    }

    /**
     * 导航结果
     */
    @Data
    public static class NavigationResult {
        private String intent;
        private String page;
        private double confidence;
        private Map<String, Object> params;
        private String suggestion;

        public NavigationResult(String intent, String page, double confidence, 
                                Map<String, Object> params, String suggestion) {
            this.intent = intent;
            this.page = page;
            this.confidence = confidence;
            this.params = params;
            this.suggestion = suggestion;
        }
    }
}


