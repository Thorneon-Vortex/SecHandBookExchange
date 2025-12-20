package com.tiancai.config;

import com.tiancai.interceptor.AdminInterceptor;
import com.tiancai.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // C端拦截器
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                        "/users/login",      // 登录
                        "/users/register",   // 注册
                        "/categories",       // 获取分类（不需要认证）
                        "/ai/**",            // AI智能客服（不需要认证）
                        "/admin/**"          // B端接口由AdminInterceptor处理
                        // 注意：/listings 的GET请求在拦截器内部判断，POST需要认证
                );
        
        // B端拦截器（更严格）
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**")  // 拦截所有admin开头的接口
                .excludePathPatterns("/admin/auth/login");  // 排除登录接口
    }
}