package com.tiancai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiancai.config.DeepSeekConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek API 客户端
 */
@Slf4j
@Component
public class DeepSeekClient {

    private final DeepSeekConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Autowired
    public DeepSeekClient(DeepSeekConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 发送聊天请求
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @return AI 回复
     */
    public String chat(String systemPrompt, String userMessage) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                messages.add(systemMsg);
            }
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2048);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            Request request = new Request.Builder()
                    .url(config.getBaseUrl() + "/v1/chat/completions")
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, JSON))
                    .build();

            log.info("正在调用 DeepSeek API: {}", config.getBaseUrl());
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "无响应体";
                    log.error("DeepSeek API 调用失败, 状态码: {}, 响应: {}", response.code(), errorBody);
                    return "抱歉，AI服务暂时不可用（错误码：" + response.code() + "）";
                }

                String responseBody = response.body().string();
                log.debug("DeepSeek API 原始响应: {}", responseBody);
                
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String content = jsonNode
                        .path("choices")
                        .path(0)
                        .path("message")
                        .path("content")
                        .asText("");
                
                if (content.isEmpty()) {
                    log.warn("DeepSeek API 返回空内容，完整响应: {}", responseBody);
                    return "抱歉，AI未能生成有效回复。";
                }
                
                log.info("DeepSeek API 调用成功，回复长度: {} 字符", content.length());
                return content;
            }
        } catch (IOException e) {
            log.error("调用 DeepSeek API 异常", e);
            return "抱歉，AI服务出现异常，请稍后再试。";
        }
    }

    /**
     * 简单聊天（无系统提示词）
     */
    public String chat(String userMessage) {
        return chat(null, userMessage);
    }
}


