package com.vaibhav.aiinterviewcoach.intelligence.controller;

import com.vaibhav.aiinterviewcoach.intelligence.dto.MatchRequest;
import com.vaibhav.aiinterviewcoach.intelligence.dto.ResumeJobMatchResponse;
import com.vaibhav.aiinterviewcoach.intelligence.service.ResumeJobMatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resume-jd")
public class MatchController {

    private final ResumeJobMatchService matchService;

    public MatchController(ResumeJobMatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping("/match")
    public ResponseEntity<ResumeJobMatchResponse> matchResumeAndJd(@Valid @RequestBody MatchRequest request) {
        return ResponseEntity.ok(matchService.matchResumeWithJd(request.getResumeId(), request.getJobDescriptionId()));
    }
}

