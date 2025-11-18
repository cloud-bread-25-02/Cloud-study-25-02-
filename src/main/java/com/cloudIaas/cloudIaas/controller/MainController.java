package com.cloudIaas.cloudIaas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;


@Controller
public class MainController {

    @GetMapping("/")
    public String index(HttpServletRequest request, Model model) {
        return "main.html";
    }
}
