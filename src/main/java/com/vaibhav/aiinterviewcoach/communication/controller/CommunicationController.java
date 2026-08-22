package com.vaibhav.aiinterviewcoach.communication.controller;

import com.vaibhav.aiinterviewcoach.communication.dto.CommunicationAssessmentResponse;
import com.vaibhav.aiinterviewcoach.communication.dto.CommunicationOverviewResponse;
import com.vaibhav.aiinterviewcoach.communication.service.CommunicationAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/communication")
@RequiredArgsConstructor
public class CommunicationController {

    private final CommunicationAssessmentService assessmentService;

    @PostMapping("/assess/{interviewId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<CommunicationAssessmentResponse> assessInterview(@PathVariable Long interviewId) {
        return ResponseEntity.ok(assessmentService.assessInterview(interviewId));
    }

    @GetMapping("/interviews/{interviewId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<CommunicationAssessmentResponse> getAssessmentByInterviewId(@PathVariable Long interviewId) {
        return ResponseEntity.ok(assessmentService.getAssessmentByInterviewId(interviewId));
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<CommunicationOverviewResponse> getOverview() {
        return ResponseEntity.ok(assessmentService.getOverview());
    }
}
