package com.vaibhav.aiinterviewcoach.intelligence.controller;

import com.vaibhav.aiinterviewcoach.intelligence.dto.ResumeAnalysisResponse;
import com.vaibhav.aiinterviewcoach.intelligence.dto.ResumeRequest;
import com.vaibhav.aiinterviewcoach.intelligence.service.ClaimVerificationService;
import com.vaibhav.aiinterviewcoach.intelligence.service.ResumeAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    @GetMapping("/test")
    public String test() { return "OK"; }

    private final ResumeAnalysisService resumeAnalysisService;
    private final ClaimVerificationService claimVerificationService;

    public ResumeController(ResumeAnalysisService resumeAnalysisService, ClaimVerificationService claimVerificationService) {
        this.resumeAnalysisService = resumeAnalysisService;
        this.claimVerificationService = claimVerificationService;
    }

    @PostMapping
    public ResponseEntity<ResumeAnalysisResponse> createResume(@Valid @RequestBody ResumeRequest request) {
        return ResponseEntity.ok(resumeAnalysisService.createResume(request));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ResumeAnalysisResponse> uploadResume(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "title", required = false) String title) {
        
        String extractedText = resumeAnalysisService.parseResumeFile(file);
        
        if (title == null || title.trim().isEmpty()) {
            title = file.getOriginalFilename();
            if (title == null || title.trim().isEmpty()) {
                title = "Uploaded Resume";
            }
        }
        
        ResumeRequest request = new ResumeRequest();
        request.setTitle(title);
        request.setRawText(extractedText);
        
        return ResponseEntity.ok(resumeAnalysisService.createResume(request));
    }

    @GetMapping
    public ResponseEntity<List<ResumeAnalysisResponse>> getUserResumes() {
        return ResponseEntity.ok(resumeAnalysisService.getUserResumes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeAnalysisResponse> getResume(@PathVariable Long id) {
        return ResponseEntity.ok(resumeAnalysisService.getResume(id));
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(@PathVariable Long id) {
        return ResponseEntity.ok(resumeAnalysisService.analyzeResume(id));
    }

    @GetMapping("/{id}/analysis")
    public ResponseEntity<ResumeAnalysisResponse> getResumeAnalysis(@PathVariable Long id) {
        return ResponseEntity.ok(resumeAnalysisService.getResume(id));
    }

    @PostMapping("/{id}/claims/verify")
    public ResponseEntity<ResumeAnalysisResponse> verifyClaims(@PathVariable Long id) {
        return ResponseEntity.ok(claimVerificationService.verifyClaims(id));
    }
}

