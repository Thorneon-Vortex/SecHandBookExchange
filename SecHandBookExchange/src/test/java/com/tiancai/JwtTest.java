package com.tiancai;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

public class JwtTest {
    @Test
    public void testGenerateToken() {
        String jwt = Jwts.builder().signWith(SignatureAlgorithm.HS256, "dGlhbmNhaQ==")
                .addClaims(Map.of("id", 1, "username", "admin"))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .compact();
        System.out.println(jwt);
    }

    @Test
    public void testParseToken() {
        String s = "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJhZG1pbiIsImV4cCI6MTc1OTM5NzM1N30.G1GuyoilSnBR67P-bGGcYvypoxkkt5ffNDBiwcSo6dQ";
        Claims body = Jwts.parser().setSigningKey("dGlhbmNhaQ==")
                .parseClaimsJws(s)
                .getBody();
        System.out.println(body);
    }
}
