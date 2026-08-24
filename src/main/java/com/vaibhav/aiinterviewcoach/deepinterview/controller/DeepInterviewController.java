package com.vaibhav.aiinterviewcoach.deepinterview.controller;

import com.vaibhav.aiinterviewcoach.deepinterview.dto.*;
import com.vaibhav.aiinterviewcoach.deepinterview.service.DeepInterviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deep-interview")
public class DeepInterviewController {

    private final DeepInterviewService deepInterviewService;

    public DeepInterviewController(DeepInterviewService deepInterviewService) {
        this.deepInterviewService = deepInterviewService;
    }

    @PostMapping("/project/{projectId}/start")
    public ResponseEntity<DeepInterviewStartResponse> startProjectInterview(@PathVariable Long projectId) {
        return ResponseEntity.ok(deepInterviewService.startProjectInterview(projectId));
    }

    @PostMapping("/resume/{resumeId}/start")
    public ResponseEntity<DeepInterviewStartResponse> startResumeInterview(@PathVariable Long resumeId) {
        return ResponseEntity.ok(deepInterviewService.startResumeInterview(resumeId));
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<DeepInterviewAnswerResponse> submitAnswer(
            @PathVariable String sessionId,
            @Valid @RequestBody DeepInterviewAnswerRequest request) {
        return ResponseEntity.ok(deepInterviewService.submitAnswer(sessionId, request));
    }

    @GetMapping("/{sessionId}/result")
    public ResponseEntity<DeepInterviewResultResponse> getFinalResult(@PathVariable String sessionId) {
        return ResponseEntity.ok(deepInterviewService.getFinalResult(sessionId));
    }
}
