package com.vaibhav.aiinterviewcoach.intelligence.service;

import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.intelligence.dto.JdInterviewConfigResponse;
import com.vaibhav.aiinterviewcoach.intelligence.entity.JobDescription;
import com.vaibhav.aiinterviewcoach.intelligence.entity.JobDescriptionSkill;
import com.vaibhav.aiinterviewcoach.intelligence.repository.JobDescriptionRepository;
import com.vaibhav.aiinterviewcoach.intelligence.repository.JobDescriptionSkillRepository;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewConfigService {

    private final JobDescriptionRepository jdRepository;
    private final JobDescriptionSkillRepository jdSkillRepository;
    private final UserRepository userRepository;

    public InterviewConfigService(JobDescriptionRepository jdRepository,
                                  JobDescriptionSkillRepository jdSkillRepository,
                                  UserRepository userRepository) {
        this.jdRepository = jdRepository;
        this.jdSkillRepository = jdSkillRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public JdInterviewConfigResponse generateConfig(Long jdId) {
        User user = getAuthenticatedUser();
        JobDescription jd = jdRepository.findById(jdId).orElseThrow();

        if (!jd.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        List<JobDescriptionSkill> skills = jdSkillRepository.findByJobDescriptionId(jdId);
        List<String> focusSkills = skills.stream()
                .filter(JobDescriptionSkill::getIsRequired)
                .map(s -> s.getSkill().getName())
                .collect(Collectors.toList());

        String difficulty = "MEDIUM";
        if ("SENIOR".equalsIgnoreCase(jd.getSeniority())) difficulty = "HARD";
        else if ("FRESHER".equalsIgnoreCase(jd.getSeniority())) difficulty = "EASY";

        return JdInterviewConfigResponse.builder()
                .role(jd.getRole())
                .interviewType("JD")
                .difficulty(difficulty)
                .experienceLevel(jd.getSeniority())
                .interviewerPersona("TECHNICAL")
                .focusSkills(focusSkills)
                .build();
    }
}

