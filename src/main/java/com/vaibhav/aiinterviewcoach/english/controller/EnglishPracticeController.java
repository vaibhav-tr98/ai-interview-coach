package com.vaibhav.aiinterviewcoach.english.controller;

import com.vaibhav.aiinterviewcoach.english.dto.*;
import com.vaibhav.aiinterviewcoach.english.service.EnglishPracticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/english")
@RequiredArgsConstructor
public class EnglishPracticeController {

    private final EnglishPracticeService service;

    @PostMapping("/sessions")
    public ResponseEntity<EnglishPracticeSessionResponse> createSession(@Valid @RequestBody EnglishPracticeSessionRequest request) {
        return new ResponseEntity<>(service.createSession(request), HttpStatus.CREATED);
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<EnglishPracticeMessageResponse> addMessage(
            @PathVariable Long sessionId,
            @Valid @RequestBody EnglishPracticeMessageRequest request) {
        return new ResponseEntity<>(service.addMessage(sessionId, request), HttpStatus.CREATED);
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<EnglishPracticeSessionDetailResponse> getSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(service.getSession(sessionId));
    }

    @PostMapping("/sessions/{sessionId}/evaluate")
    public ResponseEntity<EnglishPracticeEvaluationResponse> evaluateSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(service.evaluateSession(sessionId));
    }

    @GetMapping("/progress")
    public ResponseEntity<EnglishPracticeProgressResponse> getProgress() {
        return ResponseEntity.ok(service.getProgress());
    }
    
    @GetMapping("/sessions")
    public ResponseEntity<List<EnglishPracticeSessionResponse>> getSessions() {
        return ResponseEntity.ok(service.getSessions());
    }
}
