package com.vaibhav.aiinterviewcoach.intelligence.service;

import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.intelligence.dto.ResumeJobMatchResponse;
import com.vaibhav.aiinterviewcoach.intelligence.entity.JobDescription;
import com.vaibhav.aiinterviewcoach.intelligence.entity.JobDescriptionSkill;
import com.vaibhav.aiinterviewcoach.intelligence.entity.Resume;
import com.vaibhav.aiinterviewcoach.intelligence.entity.ResumeSkill;
import com.vaibhav.aiinterviewcoach.intelligence.repository.JobDescriptionRepository;
import com.vaibhav.aiinterviewcoach.intelligence.repository.JobDescriptionSkillRepository;
import com.vaibhav.aiinterviewcoach.intelligence.repository.ResumeRepository;
import com.vaibhav.aiinterviewcoach.intelligence.repository.ResumeSkillRepository;
import com.vaibhav.aiinterviewcoach.progress.entity.UserSkillProgress;
import com.vaibhav.aiinterviewcoach.progress.repository.UserSkillProgressRepository;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResumeJobMatchService {

    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jdRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final JobDescriptionSkillRepository jdSkillRepository;
    private final UserSkillProgressRepository userSkillProgressRepository;
    private final UserRepository userRepository;

    public ResumeJobMatchService(ResumeRepository resumeRepository,
                                 JobDescriptionRepository jdRepository,
                                 ResumeSkillRepository resumeSkillRepository,
                                 JobDescriptionSkillRepository jdSkillRepository,
                                 UserSkillProgressRepository userSkillProgressRepository,
                                 UserRepository userRepository) {
        this.resumeRepository = resumeRepository;
        this.jdRepository = jdRepository;
        this.resumeSkillRepository = resumeSkillRepository;
        this.jdSkillRepository = jdSkillRepository;
        this.userSkillProgressRepository = userSkillProgressRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ResumeJobMatchResponse matchResumeWithJd(Long resumeId, Long jdId) {
        User user = getAuthenticatedUser();
        Resume resume = resumeRepository.findById(resumeId).orElseThrow();
        JobDescription jd = jdRepository.findById(jdId).orElseThrow();

        if (!resume.getUser().getId().equals(user.getId()) || !jd.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        List<ResumeSkill> resumeSkills = resumeSkillRepository.findByResumeId(resumeId);
        List<JobDescriptionSkill> jdSkills = jdSkillRepository.findByJobDescriptionId(jdId);
        List<UserSkillProgress> userProgress = userSkillProgressRepository.findByUserId(user.getId());

        Set<String> resumeSkillNames = resumeSkills.stream()
                .map(rs -> rs.getSkill().getName())
                .collect(Collectors.toSet());

        Map<String, Double> progressMap = userProgress.stream()
                .collect(Collectors.toMap(p -> p.getSkill().getName(), UserSkillProgress::getAverageScore));

        List<ResumeJobMatchResponse.SkillMatchDetail> matchedSkills = new ArrayList<>();
        List<ResumeJobMatchResponse.SkillMatchDetail> weakSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        double requiredTotalWeight = 0;
        double requiredMatchedWeight = 0;

        double preferredTotalWeight = 0;
        double preferredMatchedWeight = 0;

        for (JobDescriptionSkill jds : jdSkills) {
            String skillName = jds.getSkill().getName();
            boolean isRequired = jds.getIsRequired();
            int importance = jds.getImportance();

            if (isRequired) requiredTotalWeight += importance;
            else preferredTotalWeight += importance;

            boolean hasSkillOnResume = resumeSkillNames.contains(skillName);
            Double perf = progressMap.get(skillName);
            boolean hasDemonstrated = perf != null && perf > 0;
            String perfStr = perf == null ? "NONE" : (perf >= 70 ? "STRONG" : (perf >= 50 ? "AVERAGE" : "WEAK"));

            if (hasSkillOnResume) {
                if (isRequired) requiredMatchedWeight += importance;
                else preferredMatchedWeight += importance;

                String matchStatus;
                if (hasDemonstrated) {
                    if (perf >= 70) matchStatus = "STRONG_MATCH";
                    else if (perf >= 50) matchStatus = "PARTIAL_MATCH";
                    else matchStatus = "WEAK_MATCH";
                } else {
                    matchStatus = "PENDING_VERIFICATION"; // Has on resume, no M7 data
                }

                ResumeJobMatchResponse.SkillMatchDetail detail = ResumeJobMatchResponse.SkillMatchDetail.builder()
                        .skillName(skillName)
                        .resumeMatch("YES")
                        .demonstratedSkill(hasDemonstrated ? "YES" : "NO")
                        .currentPerformance(perfStr)
                        .matchStatus(matchStatus)
                        .build();

                if ("WEAK_MATCH".equals(matchStatus)) {
                    weakSkills.add(detail);
                } else {
                    matchedSkills.add(detail);
                }
            } else {
                missingSkills.add(skillName);
            }
        }

        int requiredPercentage = requiredTotalWeight > 0 ? (int) ((requiredMatchedWeight / requiredTotalWeight) * 100) : 100;
        int preferredPercentage = preferredTotalWeight > 0 ? (int) ((preferredMatchedWeight / preferredTotalWeight) * 100) : 100;
        
        double totalW = requiredTotalWeight + preferredTotalWeight;
        double totalM = requiredMatchedWeight + preferredMatchedWeight;
        int overallPercentage = totalW > 0 ? (int) ((totalM / totalW) * 100) : 100;

        List<String> recommendations = new ArrayList<>();
        if (!missingSkills.isEmpty()) {
            recommendations.add("Consider learning missing required skills: " + String.join(", ", missingSkills));
        }
        if (!weakSkills.isEmpty()) {
            recommendations.add("Practice your weak skills in targeted interviews.");
        }

        return ResumeJobMatchResponse.builder()
                .resumeId(resumeId)
                .jobDescriptionId(jdId)
                .role(jd.getRole())
                .requiredMatchPercentage(requiredPercentage)
                .preferredMatchPercentage(preferredPercentage)
                .overallMatchPercentage(overallPercentage)
                .matchedSkills(matchedSkills)
                .weakSkills(weakSkills)
                .missingSkills(missingSkills)
                .recommendations(recommendations)
                .build();
    }
}

