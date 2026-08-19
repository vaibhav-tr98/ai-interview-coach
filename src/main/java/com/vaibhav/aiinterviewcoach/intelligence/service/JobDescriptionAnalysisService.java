package com.vaibhav.aiinterviewcoach.intelligence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.intelligence.dto.JobDescriptionAiResponse;
import com.vaibhav.aiinterviewcoach.intelligence.dto.JobDescriptionAnalysisResponse;
import com.vaibhav.aiinterviewcoach.intelligence.dto.JobDescriptionRequest;
import com.vaibhav.aiinterviewcoach.intelligence.entity.JobDescription;
import com.vaibhav.aiinterviewcoach.intelligence.entity.JobDescriptionSkill;
import com.vaibhav.aiinterviewcoach.intelligence.prompt.IntelligencePromptBuilder;
import com.vaibhav.aiinterviewcoach.intelligence.repository.JobDescriptionRepository;
import com.vaibhav.aiinterviewcoach.intelligence.repository.JobDescriptionSkillRepository;
import com.vaibhav.aiinterviewcoach.progress.entity.Skill;
import com.vaibhav.aiinterviewcoach.progress.repository.SkillRepository;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobDescriptionAnalysisService {

    private final JobDescriptionRepository jdRepository;
    private final JobDescriptionSkillRepository jdSkillRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final ChatClient chatClient;
    private final IntelligencePromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    public JobDescriptionAnalysisService(JobDescriptionRepository jdRepository,
                                         JobDescriptionSkillRepository jdSkillRepository,
                                         SkillRepository skillRepository,
                                         UserRepository userRepository,
                                         ChatClient.Builder chatClientBuilder,
                                         IntelligencePromptBuilder promptBuilder,
                                         ObjectMapper objectMapper) {
        this.jdRepository = jdRepository;
        this.jdSkillRepository = jdSkillRepository;
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
    public JobDescriptionAnalysisResponse createJobDescription(JobDescriptionRequest request) {
        User user = getAuthenticatedUser();
        JobDescription jd = JobDescription.builder()
                .user(user)
                .title(request.getTitle())
                .rawText(request.getRawText())
                .build();
        jd = jdRepository.save(jd);
        return mapToResponse(jd);
    }

    public List<JobDescriptionAnalysisResponse> getUserJobDescriptions() {
        User user = getAuthenticatedUser();
        return jdRepository.findByUserId(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public JobDescriptionAnalysisResponse getJobDescription(Long id) {
        JobDescription jd = getAndValidateOwnership(id);
        return mapToResponse(jd);
    }

    @Transactional
    public JobDescriptionAnalysisResponse analyzeJobDescription(Long id) {
        JobDescription jd = getAndValidateOwnership(id);

        List<Skill> allowedSkills = skillRepository.findAll();
        String allowedSkillsStr = allowedSkills.stream()
                .map(Skill::getName)
                .collect(Collectors.joining(", "));

        String prompt = promptBuilder.buildJobDescriptionAnalysisPrompt(jd.getRawText(), allowedSkillsStr);

        try {
            String aiResponseStr = chatClient.prompt().user(prompt).call().content();
            if (aiResponseStr.startsWith("```json")) {
                aiResponseStr = aiResponseStr.replace("```json", "").replace("```", "").trim();
            }

            JobDescriptionAiResponse aiResponse = objectMapper.readValue(aiResponseStr, JobDescriptionAiResponse.class);

            jd.setCompany(aiResponse.getCompany());
            jd.setRole(aiResponse.getRole());
            jd.setSeniority(aiResponse.getSeniority());
            jd.setSummary(aiResponse.getSummary());
            jd = jdRepository.save(jd);

            // Delete existing skills
            jdSkillRepository.deleteByJobDescriptionId(jd.getId());
            final JobDescription finalJd = jd;

            // Save new required skills
            if (aiResponse.getRequiredSkills() != null) {
                for (JobDescriptionAiResponse.SkillItem item : aiResponse.getRequiredSkills()) {
                    allowedSkills.stream()
                            .filter(s -> s.getName().equalsIgnoreCase(item.getSkill()))
                            .findFirst()
                            .ifPresent(skill -> {
                                JobDescriptionSkill jds = JobDescriptionSkill.builder()
                                        .jobDescription(finalJd)
                                        .skill(skill)
                                        .importance(item.getImportance())
                                        .isRequired(true)
                                        .build();
                                jdSkillRepository.save(jds);
                            });
                }
            }
            
            // Save new preferred skills
            if (aiResponse.getPreferredSkills() != null) {
                for (JobDescriptionAiResponse.SkillItem item : aiResponse.getPreferredSkills()) {
                    allowedSkills.stream()
                            .filter(s -> s.getName().equalsIgnoreCase(item.getSkill()))
                            .findFirst()
                            .ifPresent(skill -> {
                                JobDescriptionSkill jds = JobDescriptionSkill.builder()
                                        .jobDescription(finalJd)
                                        .skill(skill)
                                        .importance(item.getImportance())
                                        .isRequired(false)
                                        .build();
                                jdSkillRepository.save(jds);
                            });
                }
            }

            return mapToResponse(jd);

        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze job description", e);
        }
    }

    public JobDescription getAndValidateOwnership(Long id) {
        User user = getAuthenticatedUser();
        JobDescription jd = jdRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job Description not found"));
        if (!jd.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return jd;
    }

    public JobDescriptionAnalysisResponse mapToResponse(JobDescription jd) {
        List<JobDescriptionSkill> jdSkills = jdSkillRepository.findByJobDescriptionId(jd.getId());

        List<JobDescriptionAnalysisResponse.JdSkillDto> skillDtos = jdSkills.stream()
                .map(jds -> JobDescriptionAnalysisResponse.JdSkillDto.builder()
                        .skillName(jds.getSkill().getName())
                        .importance(jds.getImportance())
                        .isRequired(jds.getIsRequired())
                        .build())
                .collect(Collectors.toList());

        return JobDescriptionAnalysisResponse.builder()
                .id(jd.getId())
                .title(jd.getTitle())
                .company(jd.getCompany())
                .role(jd.getRole())
                .seniority(jd.getSeniority())
                .summary(jd.getSummary())
                .skills(skillDtos)
                .build();
    }
}

