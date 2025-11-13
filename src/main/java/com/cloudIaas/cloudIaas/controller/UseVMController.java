package com.cloudIaas.cloudIaas.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UseVMController {
    private static final Logger logger = LogManager.getLogger(UseVMController.class);
    
    @GetMapping("/vm")
    public String vm() {
        return "login_vm.html";
    }
    
    @PostMapping("/vm/login")
    public String accessVM() {
            return "error.html";
    }
    
}
