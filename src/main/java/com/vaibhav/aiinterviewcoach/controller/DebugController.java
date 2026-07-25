package com.vaibhav.aiinterviewcoach.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @GetMapping("/env")
    public String env() {

        String key = System.getenv("OPENROUTER_API_KEY");

        return key == null
                ? "NULL"
                : "FOUND : " + key.substring(0, 15) + "...";
    }
}