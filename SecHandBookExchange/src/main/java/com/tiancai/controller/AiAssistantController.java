package com.tiancai.controller;

import com.tiancai.ai.AiAssistantService;
import com.tiancai.entity.Result;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI 智能客服控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai")
public class AiAssistantController {

    @Autowired
    private AiAssistantService aiAssistantService;

    /**
     * 智能客服对话接口
     * 
     * @param request 包含用户消息的请求
     * @return AI 响应
     */
    @PostMapping("/chat")
    public Result chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return Result.error("消息不能为空");
        }
        
        try {
            AiAssistantService.AssistantResponse response = aiAssistantService.chat(request.getMessage());
            return Result.success(response);
        } catch (Exception e) {
            log.error("AI对话异常", e);
            return Result.error("AI服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 聊天请求
     */
    @Data
    public static class ChatRequest {
        private String message;
    }
}


