package com.vaibhav.aiinterviewcoach.intelligence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.intelligence.dto.ResumeAiResponse;
import com.vaibhav.aiinterviewcoach.intelligence.dto.ResumeAnalysisResponse;
import com.vaibhav.aiinterviewcoach.intelligence.dto.ResumeRequest;
import com.vaibhav.aiinterviewcoach.intelligence.entity.Resume;
import com.vaibhav.aiinterviewcoach.intelligence.entity.ResumeSkill;
import com.vaibhav.aiinterviewcoach.intelligence.prompt.IntelligencePromptBuilder;
import com.vaibhav.aiinterviewcoach.intelligence.repository.ResumeRepository;
import com.vaibhav.aiinterviewcoach.intelligence.repository.ResumeSkillRepository;
import com.vaibhav.aiinterviewcoach.intelligence.repository.ResumeClaimRepository;
import com.vaibhav.aiinterviewcoach.intelligence.entity.ResumeClaim;
import com.vaibhav.aiinterviewcoach.progress.entity.Skill;
import com.vaibhav.aiinterviewcoach.progress.repository.SkillRepository;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeAnalysisService {

    private final ResumeRepository resumeRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final ResumeClaimRepository resumeClaimRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final ChatClient chatClient;
    private final IntelligencePromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    public ResumeAnalysisService(ResumeRepository resumeRepository,
                                 ResumeSkillRepository resumeSkillRepository,
                                 ResumeClaimRepository resumeClaimRepository,
                                 SkillRepository skillRepository,
                                 UserRepository userRepository,
                                 ChatClient.Builder chatClientBuilder,
                                 IntelligencePromptBuilder promptBuilder,
                                 ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.resumeSkillRepository = resumeSkillRepository;
        this.resumeClaimRepository = resumeClaimRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public ResumeAnalysisResponse createResume(ResumeRequest request) {
        User user = getAuthenticatedUser();
        Resume resume = Resume.builder()
                .user(user)
                .title(request.getTitle())
                .rawText(request.getRawText())
                .build();
        resume = resumeRepository.save(resume);
        return mapToResponse(resume);
    }

    public List<ResumeAnalysisResponse> getUserResumes() {
        User user = getAuthenticatedUser();
        return resumeRepository.findByUserId(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ResumeAnalysisResponse getResume(Long id) {
        Resume resume = getAndValidateOwnership(id);
        return mapToResponse(resume);
    }

    @Transactional
    public ResumeAnalysisResponse analyzeResume(Long id) {
        Resume resume = getAndValidateOwnership(id);

        List<Skill> allowedSkills = skillRepository.findAll();
        String allowedSkillsStr = allowedSkills.stream()
                .map(Skill::getName)
                .collect(Collectors.joining(", "));

        String prompt = promptBuilder.buildResumeAnalysisPrompt(resume.getRawText(), allowedSkillsStr);

        try {
            String aiResponseStr = chatClient.prompt().user(prompt).call().content();
            // clean up aiResponseStr in case of formatting
            if (aiResponseStr.startsWith("```json")) {
                aiResponseStr = aiResponseStr.replace("```json", "").replace("```", "").trim();
            }

            ResumeAiResponse aiResponse = objectMapper.readValue(aiResponseStr, ResumeAiResponse.class);

            resume.setSummary(aiResponse.getSummary());
            resume = resumeRepository.save(resume);

            // Delete existing skills
            resumeSkillRepository.deleteByResumeId(resume.getId());
            final Resume finalResume = resume;

            // Save new skills
            if (aiResponse.getSkills() != null) {
                for (ResumeAiResponse.SkillItem item : aiResponse.getSkills()) {
                    allowedSkills.stream()
                            .filter(s -> s.getName().equalsIgnoreCase(item.getSkill()))
                            .findFirst()
                            .ifPresent(skill -> {
                                ResumeSkill rs = ResumeSkill.builder()
                                        .resume(finalResume)
                                        .skill(skill)
                                        .confidence(item.getConfidence())
                                        .build();
                                resumeSkillRepository.save(rs);
                            });
                }
            }

            return mapToResponse(resume);

        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze resume", e);
        }
    }

    public Resume getAndValidateOwnership(Long id) {
        User user = getAuthenticatedUser();
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        if (!resume.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return resume;
    }

    public ResumeAnalysisResponse mapToResponse(Resume resume) {
        List<ResumeSkill> resumeSkills = resumeSkillRepository.findByResumeId(resume.getId());
        List<ResumeClaim> resumeClaims = resumeClaimRepository.findByResumeId(resume.getId());

        List<ResumeAnalysisResponse.ResumeSkillDto> skillDtos = resumeSkills.stream()
                .map(rs -> ResumeAnalysisResponse.ResumeSkillDto.builder()
                        .skillName(rs.getSkill().getName())
                        .confidence(rs.getConfidence())
                        .build())
                .collect(Collectors.toList());

        List<ResumeAnalysisResponse.ClaimDto> claimDtos = resumeClaims.stream()
                .map(rc -> ResumeAnalysisResponse.ClaimDto.builder()
                        .claimText(rc.getClaimText())
                        .status(rc.getStatus().name())
                        .verificationQuestions(rc.getVerificationQuestions())
                        .build())
                .collect(Collectors.toList());

        return ResumeAnalysisResponse.builder()
                .id(resume.getId())
                .title(resume.getTitle())
                .summary(resume.getSummary())
                .skills(skillDtos)
                .claims(claimDtos)
                .build();
    }
}

