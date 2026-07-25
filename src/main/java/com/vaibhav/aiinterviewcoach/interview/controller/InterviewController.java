package com.vaibhav.aiinterviewcoach.interview.controller;

import com.vaibhav.aiinterviewcoach.interview.dto.InterviewRequest;
import com.vaibhav.aiinterviewcoach.interview.dto.InterviewResponse;
import com.vaibhav.aiinterviewcoach.interview.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/start")
    public InterviewResponse startInterview(
            @RequestBody @Valid InterviewRequest request) {

        return interviewService.startInterview(request);
    }
}