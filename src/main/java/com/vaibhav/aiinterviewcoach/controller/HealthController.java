package com.vaibhav.aiinterviewcoach.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String root() {
        return "AI Interview Coach API is running 🚀";
    }

    @GetMapping("/api/v1/health")
    public String health() {
        return "OK";
    }
}