package com.vaibhav.aiinterviewcoach.progress.controller;

import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.progress.dto.OverallProgressResponse;
import com.vaibhav.aiinterviewcoach.progress.dto.SkillProgressResponse;
import com.vaibhav.aiinterviewcoach.progress.service.ProgressService;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<OverallProgressResponse> getOverallProgress() {
        User user = getCurrentUser();
        return ResponseEntity.ok(progressService.getOverallProgress(user));
    }

    @GetMapping("/skills")
    public ResponseEntity<List<SkillProgressResponse>> getPracticedSkills() {
        User user = getCurrentUser();
        return ResponseEntity.ok(progressService.getPracticedSkills(user));
    }

    @GetMapping("/skills/{skillName}")
    public ResponseEntity<SkillProgressResponse> getSkillProgress(@PathVariable String skillName) {
        User user = getCurrentUser();
        return ResponseEntity.ok(progressService.getSkillProgress(user, skillName));
    }
}
