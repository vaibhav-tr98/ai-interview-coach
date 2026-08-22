package com.vaibhav.aiinterviewcoach.communication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaibhav.aiinterviewcoach.ai.AiService;
import com.vaibhav.aiinterviewcoach.communication.dto.CommunicationAiResponse;
import com.vaibhav.aiinterviewcoach.communication.dto.CommunicationAssessmentResponse;
import com.vaibhav.aiinterviewcoach.communication.dto.CommunicationOverviewResponse;
import com.vaibhav.aiinterviewcoach.communication.entity.CommunicationAssessment;
import com.vaibhav.aiinterviewcoach.communication.repository.CommunicationAssessmentRepository;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.interview.entity.Interview;
import com.vaibhav.aiinterviewcoach.interview.entity.QuestionAnswer;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewStatus;
import com.vaibhav.aiinterviewcoach.interview.repository.InterviewRepository;
import com.vaibhav.aiinterviewcoach.interview.repository.QuestionAnswerRepository;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunicationAssessmentService {

    private final CommunicationAssessmentRepository assessmentRepository;
    private final InterviewRepository interviewRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final UserRepository userRepository;
    private final CommunicationPromptBuilder promptBuilder;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Transactional
    public CommunicationAssessmentResponse assessInterview(Long interviewId) {
        User user = getAuthenticatedUser();
        
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found"));

        if (!interview.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized to access this interview");
        }

        if (interview.getStatus() != InterviewStatus.COMPLETED) {
            throw new IllegalStateException("Cannot assess an incomplete interview");
        }

        if (assessmentRepository.existsByInterviewId(interviewId)) {
            return getAssessmentByInterviewId(interviewId);
        }

        List<QuestionAnswer> qas = questionAnswerRepository.findBySession_Interview_UserId(user.getId())
                                    .stream()
                                    .filter(qa -> qa.getSession().getInterview().getId().equals(interviewId))
                                    .toList();

        if (qas.isEmpty()) {
            throw new IllegalStateException("Interview has no answers to evaluate");
        }

        List<String> questions = new ArrayList<>();
        List<String> answers = new ArrayList<>();
        for (QuestionAnswer qa : qas) {
            questions.add(qa.getQuestion());
            answers.add(qa.getAnswer());
        }

        String prompt = promptBuilder.buildAssessmentPrompt(questions, answers);
        String aiResponseText = aiService.askGemini(prompt);
        if (aiResponseText.startsWith("```json")) {
            aiResponseText = aiResponseText.substring(7);
            if (aiResponseText.endsWith("```")) {
                aiResponseText = aiResponseText.substring(0, aiResponseText.length() - 3);
            }
        } else if (aiResponseText.startsWith("```")) {
            aiResponseText = aiResponseText.substring(3);
            if (aiResponseText.endsWith("```")) {
                aiResponseText = aiResponseText.substring(0, aiResponseText.length() - 3);
            }
        }
        
        CommunicationAiResponse aiResponse;
        try {
            aiResponse = objectMapper.readValue(aiResponseText, CommunicationAiResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", aiResponseText, e);
            throw new RuntimeException("Failed to generate communication assessment");
        }

        CommunicationAssessment assessment = CommunicationAssessment.builder()
                .user(user)
                .interview(interview)
                .overallScore(aiResponse.getOverallScore())
                .clarityScore(aiResponse.getClarityScore())
                .relevanceScore(aiResponse.getRelevanceScore())
                .completenessScore(aiResponse.getCompletenessScore())
                .concisenessScore(aiResponse.getConcisenessScore())
                .vocabularyScore(aiResponse.getVocabularyScore())
                .confidenceScore(aiResponse.getConfidenceScore())
                .fillerWordCount(aiResponse.getFillerWordCount())
                .repetitionCount(aiResponse.getRepetitionCount())
                .strengths(String.join("||", aiResponse.getStrengths() != null ? aiResponse.getStrengths() : List.of()))
                .weaknesses(String.join("||", aiResponse.getWeaknesses() != null ? aiResponse.getWeaknesses() : List.of()))
                .recommendations(String.join("||", aiResponse.getRecommendations() != null ? aiResponse.getRecommendations() : List.of()))
                .build();

        assessment = assessmentRepository.save(assessment);
        
        return mapToResponse(assessment);
    }

    @Transactional(readOnly = true)
    public CommunicationAssessmentResponse getAssessmentByInterviewId(Long interviewId) {
        User user = getAuthenticatedUser();
        
        CommunicationAssessment assessment = assessmentRepository.findByInterviewIdAndUser(interviewId, user)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
                
        return mapToResponse(assessment);
    }

    @Transactional(readOnly = true)
    public CommunicationOverviewResponse getOverview() {
        User user = getAuthenticatedUser();
        List<CommunicationAssessment> history = assessmentRepository.findAllByUserOrderByAssessedAtDesc(user);
        
        if (history.isEmpty()) {
            return CommunicationOverviewResponse.builder()
                    .totalAssessments(0)
                    .averageOverallScore(0.0)
                    .averageClarityScore(0.0)
                    .averageConfidenceScore(0.0)
                    .history(List.of())
                    .build();
        }
        
        double avgOverall = history.stream().mapToInt(CommunicationAssessment::getOverallScore).average().orElse(0.0);
        double avgClarity = history.stream().mapToInt(CommunicationAssessment::getClarityScore).average().orElse(0.0);
        double avgConfidence = history.stream().mapToInt(CommunicationAssessment::getConfidenceScore).average().orElse(0.0);
        
        return CommunicationOverviewResponse.builder()
                .totalAssessments(history.size())
                .averageOverallScore(avgOverall)
                .averageClarityScore(avgClarity)
                .averageConfidenceScore(avgConfidence)
                .history(history.stream().map(this::mapToResponse).toList())
                .build();
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private CommunicationAssessmentResponse mapToResponse(CommunicationAssessment entity) {
        return CommunicationAssessmentResponse.builder()
                .id(entity.getId())
                .interviewId(entity.getInterview().getId())
                .overallScore(entity.getOverallScore())
                .clarityScore(entity.getClarityScore())
                .relevanceScore(entity.getRelevanceScore())
                .completenessScore(entity.getCompletenessScore())
                .concisenessScore(entity.getConcisenessScore())
                .vocabularyScore(entity.getVocabularyScore())
                .confidenceScore(entity.getConfidenceScore())
                .fillerWordCount(entity.getFillerWordCount())
                .repetitionCount(entity.getRepetitionCount())
                .strengths(entity.getStrengths() != null && !entity.getStrengths().isEmpty() ? List.of(entity.getStrengths().split("\\|\\|")) : List.of())
                .weaknesses(entity.getWeaknesses() != null && !entity.getWeaknesses().isEmpty() ? List.of(entity.getWeaknesses().split("\\|\\|")) : List.of())
                .recommendations(entity.getRecommendations() != null && !entity.getRecommendations().isEmpty() ? List.of(entity.getRecommendations().split("\\|\\|")) : List.of())
                .assessedAt(entity.getAssessedAt())
                .build();
    }
}
