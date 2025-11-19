package com.cloudIaas.controller;

import com.cloudIaas.jwt_util.JWTManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final JWTManager jwtManager;

    @PostMapping("/login")
    public String login(@RequestParam String username) {
        return jwtManager.generateToken(username);
    }
}
