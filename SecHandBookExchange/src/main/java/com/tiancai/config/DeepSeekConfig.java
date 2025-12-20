package com.tiancai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek API 配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {
    
    private String apiKey;
    private String baseUrl = "https://api.deepseek.com";
    private String model = "deepseek-chat";
}


