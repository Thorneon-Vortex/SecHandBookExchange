package com.tiancai.interceptor;

import com.tiancai.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员权限拦截器
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取Token
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"请先登录\"}");
            return false;
        }

        token = token.substring(7);

        try {
            Claims claims = JwtUtils.parseToken(token);
            
            // 验证是否是管理员token
            String type = claims.get("type", String.class);
            if (!"admin".equals(type)) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":0,\"msg\":\"权限不足\"}");
                return false;
            }
            
            // 将管理员信息存储到请求属性中（可选）
            Integer adminId = claims.get("adminId", Integer.class);
            String role = claims.get("role", String.class);
            request.setAttribute("adminId", adminId);
            request.setAttribute("adminRole", role);
            
        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"Token无效\"}");
            return false;
        }
        
        return true;
    }
}


