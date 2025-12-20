package com.tiancai.ai;

import com.tiancai.entity.Category;
import com.tiancai.mapper.CategoryMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 智能助手服务
 * 统一入口，整合 RAG 问答 + 智能导航（含智能搜索）
 * 
 * 适用于 C 端用户场景：
 * - FAQ: 回答使用问题
 * - SEARCH: 搜索书籍（跳转到搜索结果页，支持按分类或关键词）
 * - NAVIGATE: 页面导航
 * - CHAT: 普通闲聊
 */
@Slf4j
@Service
public class AiAssistantService {

    @Autowired
    private DeepSeekClient deepSeekClient;

    @Autowired
    private RagService ragService;

    @Autowired
    private NavigationService navigationService;

    @Autowired
    private CategoryMapper categoryMapper;

    private static final String INTENT_ROUTER_PROMPT = """
            你是一个意图分类器。根据用户输入，判断属于以下哪种类型：
            
            1. FAQ - 关于平台使用的问题（如：怎么注册、怎么发布书籍、什么是信誉分、怎么购买、怎么搜索）
            2. SEARCH - 搜索书籍请求（如：有没有数据库的书、找一本高数教材、搜计算机网络）
            3. NAVIGATE - 页面导航请求（如：我想卖书、去看看我的订单、打开个人中心、去首页）
            4. CHAT - 普通闲聊（如：你好、谢谢、再见、你是谁）
            
            注意：
            - 如果用户在询问"怎么做"、"如何"、"什么是"、"为什么"等问题，是FAQ
            - 如果用户在找特定的书、搜索书籍，是SEARCH
            - 如果用户表达要去某个地方、想做某个动作（不涉及搜索），是NAVIGATE
            
            只返回类型标识（FAQ/SEARCH/NAVIGATE/CHAT），不要其他任何文字。
            """;

    /**
     * 智能对话入口
     */
    public AssistantResponse chat(String userMessage) {
        log.info("收到用户消息: {}", userMessage);
        
        // 1. 意图识别
        String intent = classifyIntent(userMessage);
        log.info("识别到意图: {}", intent);

        // 2. 根据意图路由到不同模块
        return switch (intent.toUpperCase().trim()) {
            case "FAQ" -> handleFaq(userMessage);
            case "SEARCH" -> handleSearch(userMessage);
            case "NAVIGATE" -> handleNavigate(userMessage);
            default -> handleChat(userMessage);
        };
    }

    /**
     * 意图分类
     */
    private String classifyIntent(String userMessage) {
        String response = deepSeekClient.chat(INTENT_ROUTER_PROMPT, userMessage);
        return response.trim();
    }

    /**
     * 处理 FAQ 问题 - 使用 RAG
     */
    private AssistantResponse handleFaq(String userMessage) {
        String answer = ragService.answer(userMessage);
        return new AssistantResponse("faq", answer, null);
    }

    /**
     * 处理搜索请求 - 智能区分分类搜索和关键词搜索
     */
    private AssistantResponse handleSearch(String userMessage) {
        // 1. 先判断是分类搜索还是关键词搜索
        SearchIntent searchIntent = analyzeSearchIntent(userMessage);
        log.info("搜索意图: type={}, value={}", searchIntent.type, searchIntent.value);
        
        Map<String, Object> params = new HashMap<>();
        String suggestion;
        
        if ("category".equals(searchIntent.type)) {
            // 分类搜索：匹配分类名称获取 categoryId
            Integer categoryId = matchCategory(searchIntent.value);
            if (categoryId != null) {
                params.put("categoryId", categoryId);
                suggestion = String.format("帮您筛选「%s」分类的书籍 📚", searchIntent.value);
            } else {
                // 分类不存在，降级为关键词搜索
                params.put("keyword", searchIntent.value);
                suggestion = String.format("未找到「%s」分类，已改为关键词搜索 🔍", searchIntent.value);
            }
        } else {
            // 关键词搜索
            params.put("keyword", searchIntent.value);
            suggestion = String.format("帮您搜索「%s」相关的书籍 🔍", searchIntent.value);
        }
        
        NavigationService.NavigationResult navResult = new NavigationService.NavigationResult(
                "搜索书籍",
                "listings",
                0.95,
                params,
                suggestion
        );
        
        return new AssistantResponse("navigate", navResult.getSuggestion(), navResult);
    }

    /**
     * 分析搜索意图：分类搜索 or 关键词搜索
     */
    private SearchIntent analyzeSearchIntent(String userMessage) {
        // 获取所有分类名称
        List<Category> categories = categoryMapper.findAllCategories();
        String categoryList = categories.stream()
                .map(Category::getCategoryName)
                .reduce((a, b) -> a + "、" + b)
                .orElse("");
        
        String prompt = String.format("""
                分析用户的搜索请求，判断是按【分类】搜索还是按【关键词】搜索。
                
                当前系统支持的书籍分类有：%s
                
                判断规则：
                1. 如果用户提到"XX类"、"XX类的书"、"XX类书籍"，且XX在分类列表中，则是分类搜索
                2. 如果用户提到的是具体书名、课程名、技术名词等，则是关键词搜索
                
                返回格式（只返回这一行，不要其他文字）：
                type:value
                
                其中 type 是 category 或 keyword，value 是提取的分类名或关键词。
                
                示例：
                - "有没有艺术类的书" → category:艺术
                - "计算机类书籍" → category:计算机
                - "有没有数据库的书" → keyword:数据库
                - "找一本高等数学" → keyword:高等数学
                - "Java编程的书" → keyword:Java编程
                """, categoryList);
        
        String response = deepSeekClient.chat(prompt, userMessage).trim();
        log.info("搜索意图分析结果: {}", response);
        
        // 解析响应
        if (response.contains(":")) {
            String[] parts = response.split(":", 2);
            if (parts.length == 2) {
                return new SearchIntent(parts[0].trim().toLowerCase(), parts[1].trim());
            }
        }
        
        // 默认为关键词搜索
        return new SearchIntent("keyword", extractFallbackKeyword(userMessage));
    }
    
    /**
     * 匹配分类名称，返回分类ID
     */
    private Integer matchCategory(String categoryName) {
        List<Category> categories = categoryMapper.findAllCategories();
        
        // 精确匹配
        for (Category category : categories) {
            if (category.getCategoryName().equals(categoryName)) {
                return category.getCategoryId();
            }
        }
        
        // 模糊匹配（包含关系）
        for (Category category : categories) {
            if (category.getCategoryName().contains(categoryName) || 
                categoryName.contains(category.getCategoryName())) {
                return category.getCategoryId();
            }
        }
        
        return null;
    }
    
    /**
     * 降级：提取简单关键词
     */
    private String extractFallbackKeyword(String userMessage) {
        // 移除常见的搜索词汇，提取核心内容
        String cleaned = userMessage
                .replaceAll("有没有|有什么|找一本|搜一下|帮我|搜索|查找|书籍|的书|书", "")
                .trim();
        return cleaned.isEmpty() ? userMessage : cleaned;
    }
    
    /**
     * 搜索意图
     */
    private static class SearchIntent {
        String type;  // category 或 keyword
        String value; // 分类名或关键词
        
        SearchIntent(String type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    /**
     * 处理页面导航
     */
    private AssistantResponse handleNavigate(String userMessage) {
        NavigationService.NavigationResult result = navigationService.analyzeAndNavigate(userMessage);
        return new AssistantResponse("navigate", result.getSuggestion(), result);
    }

    /**
     * 处理普通闲聊
     */
    private AssistantResponse handleChat(String userMessage) {
        String systemPrompt = """
                你是"校园二手书交易平台"的智能客服小助手，名叫"小书"。
                请友好地回应用户，可以适当使用emoji。
                回复要简短（不超过50字）。
                如果用户需要帮助，可以引导他们询问具体问题，比如：
                - 怎么发布书籍？
                - 怎么购买书籍？
                - 什么是信誉分？
                """;
        String response = deepSeekClient.chat(systemPrompt, userMessage);
        return new AssistantResponse("chat", response, null);
    }

    /**
     * 助手响应
     */
    @Data
    public static class AssistantResponse {
        private String type;  // faq, navigate, chat
        private String message;
        private NavigationService.NavigationResult navigation;  // 导航/搜索信息

        public AssistantResponse(String type, String message, 
                                 NavigationService.NavigationResult navigation) {
            this.type = type;
            this.message = message;
            this.navigation = navigation;
        }
    }
}

