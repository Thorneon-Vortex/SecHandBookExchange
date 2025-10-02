package com.tiancai.config;

import com.tiancai.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                        "/users/login",      // 登录
                        "/users/register",   // 注册
                        "/listings",         // 浏览书籍列表（不需要认证）
                        "/listings/*",       // 获取书籍详情（不需要认证）
                        "/categories"        // 获取分类（不需要认证）
                ); 
    }
}