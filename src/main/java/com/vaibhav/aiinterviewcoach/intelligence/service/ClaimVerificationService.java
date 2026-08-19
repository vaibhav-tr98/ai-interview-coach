package com.vaibhav.aiinterviewcoach.intelligence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.intelligence.dto.ClaimAiResponse;
import com.vaibhav.aiinterviewcoach.intelligence.dto.ResumeAnalysisResponse;
import com.vaibhav.aiinterviewcoach.intelligence.entity.Resume;
import com.vaibhav.aiinterviewcoach.intelligence.entity.ResumeClaim;
import com.vaibhav.aiinterviewcoach.intelligence.enums.ClaimStatus;
import com.vaibhav.aiinterviewcoach.intelligence.prompt.IntelligencePromptBuilder;
import com.vaibhav.aiinterviewcoach.intelligence.repository.ResumeClaimRepository;
import com.vaibhav.aiinterviewcoach.intelligence.repository.ResumeRepository;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClaimVerificationService {

    private final ResumeRepository resumeRepository;
    private final ResumeClaimRepository resumeClaimRepository;
    private final UserRepository userRepository;
    private final ChatClient chatClient;
    private final IntelligencePromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;
    private final ResumeAnalysisService resumeAnalysisService;

    public ClaimVerificationService(ResumeRepository resumeRepository,
                                    ResumeClaimRepository resumeClaimRepository,
                                    UserRepository userRepository,
                                    ChatClient.Builder chatClientBuilder,
                                    IntelligencePromptBuilder promptBuilder,
                                    ObjectMapper objectMapper,
                                    ResumeAnalysisService resumeAnalysisService) {
        this.resumeRepository = resumeRepository;
        this.resumeClaimRepository = resumeClaimRepository;
        this.userRepository = userRepository;
        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
        this.resumeAnalysisService = resumeAnalysisService;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public ResumeAnalysisResponse verifyClaims(Long resumeId) {
        User user = getAuthenticatedUser();
        Resume resume = resumeRepository.findById(resumeId).orElseThrow();
        if (!resume.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        String prompt = promptBuilder.buildClaimVerificationPrompt(resume.getRawText());

        try {
            String aiResponseStr = chatClient.prompt().user(prompt).call().content();
            if (aiResponseStr.startsWith("```json")) {
                aiResponseStr = aiResponseStr.replace("```json", "").replace("```", "").trim();
            }

            ClaimAiResponse aiResponse = objectMapper.readValue(aiResponseStr, ClaimAiResponse.class);

            // clear old claims
            resumeClaimRepository.deleteByResumeId(resumeId);

            if (aiResponse.getClaims() != null) {
                for (ClaimAiResponse.ClaimItem item : aiResponse.getClaims()) {
                    ClaimStatus status;
                    try {
                        status = ClaimStatus.valueOf(item.getStatus());
                    } catch (Exception e) {
                        status = ClaimStatus.NEEDS_VERIFICATION;
                    }
                    ResumeClaim claim = ResumeClaim.builder()
                            .resume(resume)
                            .claimText(item.getClaimText())
                            .status(status)
                            .verificationQuestions(item.getVerificationQuestions())
                            .build();
                    resumeClaimRepository.save(claim);
                }
            }

            return resumeAnalysisService.mapToResponse(resume);

        } catch (Exception e) {
            throw new RuntimeException("Failed to verify claims", e);
        }
    }
}

