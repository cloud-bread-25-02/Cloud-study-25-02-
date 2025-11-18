package com.cloudIaas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @GetMapping("/login")
    public String login(Model model) {
        return "admin_login.html";
    }
    
    @PostMapping("/login")
    public String tryLogin() {
        return "redirect:/admin/login";
    }

    @GetMapping("")
    public String getMethodName() {
        return "admin_main.html";
    }   

    @GetMapping("/requests")
    public String requestsAccount() {

        return "admin_requests.html";
    }
    
    @PostMapping("/requests/approve")
    public String approveAccount() {
        return "error.html";
    }
    
    @PostMapping("/requests/reject")
    public String rejectAccount() {
        return "error.html";
    }

    @GetMapping("/users")
    public String userList() {
        return "admin_users.html";
    }

    @PostMapping("/users/delete")
    public String deleteUser() {
        return "error.html";
    }

    @GetMapping("/vmlogs")
    public String showVmLogs() {
        return "admin_containers.html";
    }

    @PostMapping("/vmlogs/stop")
    public String stopContainer() {
        return "redirect:/admin/vmlogs";
    }

    @PostMapping("vmlogs/start")
    public String startContainer() {
        return "redirect:/admin/vmlogs";
    }
}
