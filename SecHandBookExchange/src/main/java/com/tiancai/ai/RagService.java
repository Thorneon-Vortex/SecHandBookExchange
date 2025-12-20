package com.tiancai.ai;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG 服务 - 基于文档的智能问答
 * 使用简化的关键词匹配进行文档检索
 */
@Slf4j
@Service
public class RagService {

    @Autowired
    private DeepSeekClient deepSeekClient;

    // 文档块存储
    private final List<DocumentChunk> documentChunks = new ArrayList<>();

    private static final String SYSTEM_PROMPT = """
            你是"校园二手书交易平台"的智能客服助手。
            
            你的职责是：
            1. 解答用户关于平台使用的问题
            2. 帮助用户了解如何发布、购买书籍
            3. 解释订单流程和信誉积分机制
            
            请基于以下参考资料回答用户问题。如果资料中没有相关信息，请根据你的理解给出合理的回答。
            
            参考资料：
            %s
            
            注意：
            - 回答要简洁明了
            - 如果是操作步骤，请分点说明
            - 可以适当使用emoji增加亲和力
            - 回答限制在200字以内
            """;

    /**
     * 初始化时加载文档
     */
    @PostConstruct
    public void loadDocuments() {
        try {
            // 加载使用指南
            loadDocument("docs/使用指南.md", "使用指南");
            log.info("RAG 文档加载完成，共 {} 个文档块", documentChunks.size());
        } catch (Exception e) {
            log.warn("加载文档失败，将使用内置知识: {}", e.getMessage());
            // 加载内置的知识
            loadBuiltInKnowledge();
        }
    }

    /**
     * 加载内置知识（当外部文档不可用时）
     */
    private void loadBuiltInKnowledge() {
        String[] builtInDocs = {
                "## 注册与登录\n如何注册账号？点击页面右上角的【注册】按钮，填写学号、昵称、密码和联系方式，点击【注册】完成。如何登录？点击【登录】按钮，输入学号和密码即可。",
                "## 发布书籍\n如何发布一本书？登录后点击【发布书籍】，填写书籍信息（ISBN、书名、作者、出版社），选择分类，上传封面图片，选择发布类型（出售或赠送），如果是出售需设置价格，描述书籍新旧程度，点击【发布】完成。",
                "## 购买书籍\n如何搜索书籍？在首页或书籍市场使用搜索框，可以输入书名、作者或ISBN，也可以按分类筛选或按价格排序。如何购买？找到书籍后点击进入详情页，查看信息后点击【立即购买】创建订单，再通过联系方式与卖家约定交易。",
                "## 订单管理\n如何查看订单？点击【我的订单】，可以切换【我买的】和【我卖的】。订单状态包括：待确认（等待线下交易）、已完成（双方确认完成）、已取消（订单被取消）。",
                "## 信誉积分\n什么是信誉分？衡量用户可信度的指标，初始100分。完成一笔交易双方各得5分。高信誉分的用户更受信任，可作为交易参考。"
        };

        for (String doc : builtInDocs) {
            documentChunks.add(new DocumentChunk(doc, "内置知识", extractKeywords(doc)));
        }
        log.info("加载内置知识完成，共 {} 个文档块", documentChunks.size());
    }

    /**
     * 加载文档文件
     */
    private void loadDocument(String path, String source) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IOException("文档不存在: " + path);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String content = reader.lines().collect(Collectors.joining("\n"));
            
            // 按二级标题切分
            String[] sections = content.split("(?=## )");
            
            for (String section : sections) {
                if (section.trim().isEmpty()) continue;
                
                // 进一步按三级标题切分
                String[] subSections = section.split("(?=### )");
                
                for (String subSection : subSections) {
                    if (subSection.trim().length() > 50) { // 只保留有实质内容的块
                        Set<String> keywords = extractKeywords(subSection);
                        documentChunks.add(new DocumentChunk(subSection.trim(), source, keywords));
                    }
                }
            }
        }
    }

    /**
     * 提取关键词
     */
    private Set<String> extractKeywords(String text) {
        Set<String> keywords = new HashSet<>();
        
        // 常用关键词映射
        Map<String, List<String>> keywordMap = new HashMap<>();
        keywordMap.put("注册", Arrays.asList("注册", "账号", "创建账号", "新用户"));
        keywordMap.put("登录", Arrays.asList("登录", "登陆", "进入", "账号"));
        keywordMap.put("发布", Arrays.asList("发布", "上架", "卖书", "出售", "赠送", "发布书籍"));
        keywordMap.put("购买", Arrays.asList("购买", "买书", "下单", "订购"));
        keywordMap.put("搜索", Arrays.asList("搜索", "查找", "找书", "搜书"));
        keywordMap.put("订单", Arrays.asList("订单", "交易", "买卖"));
        keywordMap.put("信誉", Arrays.asList("信誉", "积分", "信誉分", "评分"));
        keywordMap.put("下架", Arrays.asList("下架", "删除", "取消发布"));
        keywordMap.put("价格", Arrays.asList("价格", "多少钱", "定价", "费用"));
        keywordMap.put("联系", Arrays.asList("联系", "QQ", "微信", "联系方式"));

        String lowerText = text.toLowerCase();
        for (Map.Entry<String, List<String>> entry : keywordMap.entrySet()) {
            for (String kw : entry.getValue()) {
                if (lowerText.contains(kw.toLowerCase())) {
                    keywords.add(entry.getKey());
                    break;
                }
            }
        }
        
        return keywords;
    }

    /**
     * 检索相关文档
     */
    private List<DocumentChunk> retrieveRelevant(String query, int topK) {
        Set<String> queryKeywords = extractKeywords(query);
        
        // 计算每个文档块的相关性分数
        List<Map.Entry<DocumentChunk, Integer>> scored = new ArrayList<>();
        
        for (DocumentChunk chunk : documentChunks) {
            int score = 0;
            for (String keyword : queryKeywords) {
                if (chunk.keywords.contains(keyword)) {
                    score += 2;
                }
            }
            // 额外检查原文中是否包含查询词
            String lowerQuery = query.toLowerCase();
            String lowerContent = chunk.content.toLowerCase();
            String[] queryWords = lowerQuery.split("[\\s，。？！、]+");
            for (String word : queryWords) {
                if (word.length() >= 2 && lowerContent.contains(word)) {
                    score += 1;
                }
            }
            
            if (score > 0) {
                scored.add(Map.entry(chunk, score));
            }
        }
        
        // 按分数排序
        scored.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        // 返回 top K
        return scored.stream()
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * RAG 问答
     */
    public String answer(String question) {
        // 检索相关文档
        List<DocumentChunk> relevantDocs = retrieveRelevant(question, 3);
        
        // 构建上下文
        String context;
        if (relevantDocs.isEmpty()) {
            context = "暂无相关资料，请根据通用知识回答。";
        } else {
            context = relevantDocs.stream()
                    .map(doc -> doc.content)
                    .collect(Collectors.joining("\n\n---\n\n"));
        }
        
        // 调用 DeepSeek
        String systemPrompt = String.format(SYSTEM_PROMPT, context);
        return deepSeekClient.chat(systemPrompt, question);
    }

    /**
     * 文档块类
     */
    private static class DocumentChunk {
        String content;
        String source;
        Set<String> keywords;

        DocumentChunk(String content, String source, Set<String> keywords) {
            this.content = content;
            this.source = source;
            this.keywords = keywords;
        }
    }
}

