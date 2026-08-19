package com.vaibhav.aiinterviewcoach.intelligence.controller;

import com.vaibhav.aiinterviewcoach.intelligence.dto.JdInterviewConfigResponse;
import com.vaibhav.aiinterviewcoach.intelligence.dto.JobDescriptionAnalysisResponse;
import com.vaibhav.aiinterviewcoach.intelligence.dto.JobDescriptionRequest;
import com.vaibhav.aiinterviewcoach.intelligence.service.InterviewConfigService;
import com.vaibhav.aiinterviewcoach.intelligence.service.JobDescriptionAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-descriptions")
public class JobDescriptionController {

    private final JobDescriptionAnalysisService jdAnalysisService;
    private final InterviewConfigService interviewConfigService;

    public JobDescriptionController(JobDescriptionAnalysisService jdAnalysisService, InterviewConfigService interviewConfigService) {
        this.jdAnalysisService = jdAnalysisService;
        this.interviewConfigService = interviewConfigService;
    }

    @PostMapping
    public ResponseEntity<JobDescriptionAnalysisResponse> createJobDescription(@Valid @RequestBody JobDescriptionRequest request) {
        return ResponseEntity.ok(jdAnalysisService.createJobDescription(request));
    }

    @GetMapping
    public ResponseEntity<List<JobDescriptionAnalysisResponse>> getUserJobDescriptions() {
        return ResponseEntity.ok(jdAnalysisService.getUserJobDescriptions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDescriptionAnalysisResponse> getJobDescription(@PathVariable Long id) {
        return ResponseEntity.ok(jdAnalysisService.getJobDescription(id));
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<JobDescriptionAnalysisResponse> analyzeJobDescription(@PathVariable Long id) {
        return ResponseEntity.ok(jdAnalysisService.analyzeJobDescription(id));
    }

    @GetMapping("/{id}/analysis")
    public ResponseEntity<JobDescriptionAnalysisResponse> getJobDescriptionAnalysis(@PathVariable Long id) {
        return ResponseEntity.ok(jdAnalysisService.getJobDescription(id));
    }

    @GetMapping("/{id}/interview-config")
    public ResponseEntity<JdInterviewConfigResponse> getInterviewConfig(@PathVariable Long id) {
        return ResponseEntity.ok(interviewConfigService.generateConfig(id));
    }
}

