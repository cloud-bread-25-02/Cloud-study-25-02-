package com.cloudIaas.jwt_util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JWTManager {
    
    // JWT 서명에 사용할 고정된 비밀 키
    private static final String SECRET_KEY = "cloudIaas-secure-key-for-jwt-signing-and-verification-with-long-length";

    // Key 객체를 생성
    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    
    // 토큰 만료 기간
    private final long EXPIRATION_TIME = 1000 * 60 * 60; //1시간

    //토큰을 지급
    public String generateToken(String username) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME);
        
        return Jwts.builder()
                .setSubject(username)      
                .setIssuedAt(now)           
                .setExpiration(expirationDate) 
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 이름 추출
    public String getUsername(String token) throws JwtException {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
                
        return claims.getSubject();
    }
    
    //유효성 검사하는 과정
    public boolean validateToken(String token) {
        try {
            // 시간(만료 여부) 확인하는 과정
            Jws<Claims> claims = Jwts.parserBuilder()
                                .setSigningKey(key)
                                .build()
                                .parseClaimsJws(token);
            Date now = new Date();
            Date expirationDate = claims.getBody().getExpiration();
            if (now.getTime() > expirationDate.getTime()) {
                JwtException a = new JwtException(new String());
                throw new JwtException("만료되었습니다"); //이게 맞나...? 미안해 (catch 구문으로)

                // throw (예외 객체);
                // 예외클래스란: 최상위 예외클래스인 Exection을 상속받은 모든 클래스
            }
            else {
                return true;
            }

        } catch (JwtException e) {
            // 토큰 검증에 실패한 경우로 넘어감
            System.err.println("JWT 인증 실패하였습니다. " + e.getMessage());
            return false;
        }
    }
}