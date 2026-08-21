package com.vaibhav.aiinterviewcoach.planner.controller;

import com.vaibhav.aiinterviewcoach.planner.dto.StudyPlanResponse;
import com.vaibhav.aiinterviewcoach.planner.dto.StudyTaskResponse;
import com.vaibhav.aiinterviewcoach.planner.service.StudyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/planner")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @PostMapping("/generate")
    public ResponseEntity<StudyPlanResponse> generatePlan(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "Software Engineer") String targetRole) {
        String email = authentication.getName();
        StudyPlanResponse response = studyPlanService.generatePlan(email, targetRole);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<StudyPlanResponse> getActivePlan(Authentication authentication) {
        String email = authentication.getName();
        StudyPlanResponse response = studyPlanService.getActivePlan(email);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/tasks/{taskId}/complete")
    public ResponseEntity<StudyTaskResponse> completeTask(
            Authentication authentication,
            @PathVariable Long taskId) {
        String email = authentication.getName();
        StudyTaskResponse response = studyPlanService.completeTask(email, taskId);
        return ResponseEntity.ok(response);
    }
}
