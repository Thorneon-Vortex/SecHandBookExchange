package com.tiancai.interceptor;

import com.tiancai.utils.BaseContext;
import com.tiancai.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 对于 GET /listings 和 GET /listings/{id}，不需要认证
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        if ("GET".equals(method) && (requestURI.equals("/listings") || requestURI.matches("/listings/\\d+"))) {
            return true; // 放行GET请求
        }
        
        // 其他请求需要验证Token
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }

        token = token.substring(7);

        try {
            Claims claims = JwtUtils.parseToken(token);
            Integer userId = claims.get("userId", Integer.class);
            BaseContext.setCurrentId(userId);
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}