package com.vaibhav.aiinterviewcoach.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public java.util.Map<String, String> root() {
        return java.util.Map.of("message", "AI Interview Coach API is running 🚀");
    }

    @GetMapping("/api/v1/health")
    public java.util.Map<String, String> health() {
        return java.util.Map.of("status", "UP");
    }
}