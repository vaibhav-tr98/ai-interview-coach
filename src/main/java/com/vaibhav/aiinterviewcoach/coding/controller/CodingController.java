package com.vaibhav.aiinterviewcoach.coding.controller;

import com.vaibhav.aiinterviewcoach.coding.dto.*;
import com.vaibhav.aiinterviewcoach.coding.service.CodingProblemService;
import com.vaibhav.aiinterviewcoach.coding.service.CodingProgressService;
import com.vaibhav.aiinterviewcoach.coding.service.CodingSubmissionService;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/coding")
@RequiredArgsConstructor
public class CodingController {

    private final CodingProblemService problemService;
    private final CodingSubmissionService submissionService;
    private final CodingProgressService progressService;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @GetMapping("/problems")
    public ResponseEntity<List<CodingProblemDTO>> getProblems() {
        return ResponseEntity.ok(problemService.getProblems());
    }

    @GetMapping("/problems/{problemId}")
    public ResponseEntity<CodingProblemDTO> getProblem(@PathVariable Long problemId) {
        return ResponseEntity.ok(problemService.getProblem(problemId));
    }

    @PostMapping("/problems/generate")
    public ResponseEntity<CodingProblemDTO> generateProblem(@RequestBody Map<String, String> request) {
        getAuthenticatedUser(); // Verify authentication
        String topic = request.getOrDefault("topic", "ARRAYS");
        String difficulty = request.getOrDefault("difficulty", "MEDIUM");
        return ResponseEntity.ok(problemService.generateProblem(topic, difficulty));
    }

    @PostMapping("/problems/{problemId}/hint")
    public ResponseEntity<HintResponse> generateHint(@PathVariable Long problemId) {
        getAuthenticatedUser(); // Verify authentication
        String hint = problemService.generateHint(problemId);
        return ResponseEntity.ok(HintResponse.builder().hint(hint).build());
    }

    @PostMapping("/problems/{problemId}/submit")
    public ResponseEntity<CodingSubmissionDTO> submitCode(
            @PathVariable Long problemId,
            @RequestBody CodingSubmissionRequest request) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(submissionService.submitCode(user, problemId, request));
    }

    @GetMapping("/submissions")
    public ResponseEntity<List<CodingSubmissionDTO>> getSubmissions() {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(submissionService.getUserSubmissions(user));
    }

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<CodingSubmissionDTO> getSubmission(@PathVariable Long submissionId) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(submissionService.getSubmission(user, submissionId));
    }

    @GetMapping("/progress")
    public ResponseEntity<CodingProgressDTO> getProgress() {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(progressService.getProgress(user));
    }
}
