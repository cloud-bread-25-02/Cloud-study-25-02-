package com.cloudIaas.jwt_util;

import com.cloudIaas.domain.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JWTManager jwtManager;

    @Autowired
    private HttpServletRequest request;

    public UserInfo getCurrentUser() {

        String token = extractToken();
        if (token == null || !jwtManager.validateToken(token)) {
            return null;
        }

        String username = jwtManager.getUsername(token);

        UserInfo user = new UserInfo();
        user.setUserId(username);
        user.setName(username);
        user.setEmail(username + "@example.com"); // 필요하다면 DB에서 조회

        return user;
    }

    private String extractToken() {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}

