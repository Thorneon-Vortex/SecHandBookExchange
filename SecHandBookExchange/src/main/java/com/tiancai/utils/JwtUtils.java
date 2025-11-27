package com.tiancai.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 */
public class JwtUtils {

    // 密钥: Base64 编码的 "tiancai"。生产环境应更复杂并从配置文件读取。
    private static final String SIGN_KEY = "dGlhbmNhaQ==";

    // 过期时间: 1 小时 (单位: 毫秒)
    private static final Long EXPIRE_TIME = 1000L * 60 * 60;

    /**
     * 生成 JWT 令牌
     * @param claims 自定义的用户信息
     * @return JWT 字符串
     */
    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, SIGN_KEY)
                .addClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .compact();
    }

    /**
     * 解析 JWT 令牌
     * @param jwt JWT 字符串
     * @return Claims 对象，包含所有信息
     * @throws Exception 令牌无效会抛出异常
     */
    public static Claims parseToken(String jwt) throws Exception {
        return Jwts.parser()
                .setSigningKey(SIGN_KEY)
                .parseClaimsJws(jwt)
                .getBody();
    }
}