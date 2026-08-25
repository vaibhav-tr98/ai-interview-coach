package com.vaibhav.aiinterviewcoach.english.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaibhav.aiinterviewcoach.ai.AiService;
import com.vaibhav.aiinterviewcoach.english.dto.*;
import com.vaibhav.aiinterviewcoach.english.entity.EnglishPracticeEvaluation;
import com.vaibhav.aiinterviewcoach.english.entity.EnglishPracticeMessage;
import com.vaibhav.aiinterviewcoach.english.entity.EnglishPracticeSession;
import com.vaibhav.aiinterviewcoach.english.enums.EnglishPracticeStatus;
import com.vaibhav.aiinterviewcoach.english.repository.EnglishPracticeEvaluationRepository;
import com.vaibhav.aiinterviewcoach.english.repository.EnglishPracticeMessageRepository;
import com.vaibhav.aiinterviewcoach.english.repository.EnglishPracticeSessionRepository;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnglishPracticeService {

    private final EnglishPracticeSessionRepository sessionRepository;
    private final EnglishPracticeMessageRepository messageRepository;
    private final EnglishPracticeEvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    private final EnglishPracticePromptBuilder promptBuilder;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Transactional
    public EnglishPracticeSessionResponse createSession(EnglishPracticeSessionRequest request) {
        User user = getAuthenticatedUser();
        EnglishPracticeSession session = EnglishPracticeSession.builder()
                .user(user)
                .participantOneRole(request.getParticipantOneRole())
                .participantTwoRole(request.getParticipantTwoRole())
                .topic(request.getTopic())
                .status(EnglishPracticeStatus.ACTIVE)
                .build();
        
        session = sessionRepository.save(session);
        return mapToSessionResponse(session);
    }

    @Transactional
    public EnglishPracticeMessageResponse addMessage(Long sessionId, EnglishPracticeMessageRequest request) {
        User user = getAuthenticatedUser();
        EnglishPracticeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        
        if (!session.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Unauthorized to access this session");
        }
        
        if (session.getStatus() != EnglishPracticeStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active");
        }
        
        long count = messageRepository.countBySession(session);
        
        EnglishPracticeMessage message = EnglishPracticeMessage.builder()
                .session(session)
                .participantRole(request.getParticipantRole())
                .messageText(request.getMessageText())
                .sequenceNumber((int) count + 1)
                .build();
                
        message = messageRepository.save(message);
        return mapToMessageResponse(message);
    }

    @Transactional(readOnly = true)
    public EnglishPracticeSessionDetailResponse getSession(Long sessionId) {
        User user = getAuthenticatedUser();
        EnglishPracticeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
                
        if (!session.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Unauthorized to access this session");
        }
        
        List<EnglishPracticeMessage> messages = messageRepository.findBySessionOrderBySequenceNumberAsc(session);
        Optional<EnglishPracticeEvaluation> evaluationOpt = evaluationRepository.findBySession(session);
        
        return EnglishPracticeSessionDetailResponse.builder()
                .session(mapToSessionResponse(session))
                .messages(messages.stream().map(this::mapToMessageResponse).toList())
                .evaluation(evaluationOpt.map(this::mapToEvaluationResponse).orElse(null))
                .build();
    }

    @Transactional
    public EnglishPracticeEvaluationResponse evaluateSession(Long sessionId) {
        User user = getAuthenticatedUser();
        EnglishPracticeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
                
        if (!session.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Unauthorized to access this session");
        }
        
        Optional<EnglishPracticeEvaluation> existingEvaluation = evaluationRepository.findBySession(session);
        if (existingEvaluation.isPresent()) {
            return mapToEvaluationResponse(existingEvaluation.get());
        }
        
        List<EnglishPracticeMessage> messages = messageRepository.findBySessionOrderBySequenceNumberAsc(session);
        if (messages.size() < 2) {
            throw new IllegalStateException("Not enough messages to evaluate");
        }
        
        String prompt = promptBuilder.buildEvaluationPrompt(session, messages);
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
        
        EnglishPracticeEvaluationResponse aiParsedResponse;
        try {
            aiParsedResponse = objectMapper.readValue(aiResponseText, EnglishPracticeEvaluationResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", aiResponseText, e);
            throw new RuntimeException("Failed to generate English practice evaluation");
        }
        
        EnglishPracticeEvaluation evaluation = EnglishPracticeEvaluation.builder()
                .session(session)
                .grammarScore(aiParsedResponse.getGrammarScore())
                .vocabularyScore(aiParsedResponse.getVocabularyScore())
                .fluencyScore(aiParsedResponse.getFluencyScore())
                .clarityScore(aiParsedResponse.getClarityScore())
                .relevanceScore(aiParsedResponse.getRelevanceScore())
                .confidenceScore(aiParsedResponse.getConfidenceScore())
                .overallScore(aiParsedResponse.getOverallScore())
                .strengths(String.join("||", aiParsedResponse.getStrengths() != null ? aiParsedResponse.getStrengths() : List.of()))
                .improvements(String.join("||", aiParsedResponse.getImprovements() != null ? aiParsedResponse.getImprovements() : List.of()))
                .feedback(aiParsedResponse.getFeedback())
                .build();
                
        evaluation = evaluationRepository.save(evaluation);
        
        session.setStatus(EnglishPracticeStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        sessionRepository.save(session);
        
        return mapToEvaluationResponse(evaluation);
    }

    @Transactional(readOnly = true)
    public EnglishPracticeProgressResponse getProgress() {
        User user = getAuthenticatedUser();
        long totalSessions = sessionRepository.countByUser(user);
        long completedSessions = sessionRepository.countByUserAndStatus(user, EnglishPracticeStatus.COMPLETED);
        
        List<EnglishPracticeEvaluation> evaluations = evaluationRepository.findAllByUser(user);
        if (evaluations.isEmpty()) {
            return EnglishPracticeProgressResponse.builder()
                    .totalSessions(totalSessions)
                    .completedSessions(completedSessions)
                    .averageOverallScore(0)
                    .averageGrammarScore(0)
                    .averageVocabularyScore(0)
                    .averageFluencyScore(0)
                    .averageClarityScore(0)
                    .averageRelevanceScore(0)
                    .averageConfidenceScore(0)
                    .strongestArea(null)
                    .weakestArea(null)
                    .build();
        }
        
        double avgOverall = evaluations.stream().mapToInt(EnglishPracticeEvaluation::getOverallScore).average().orElse(0);
        double avgGrammar = evaluations.stream().mapToInt(EnglishPracticeEvaluation::getGrammarScore).average().orElse(0);
        double avgVocab = evaluations.stream().mapToInt(EnglishPracticeEvaluation::getVocabularyScore).average().orElse(0);
        double avgFluency = evaluations.stream().mapToInt(EnglishPracticeEvaluation::getFluencyScore).average().orElse(0);
        double avgClarity = evaluations.stream().mapToInt(EnglishPracticeEvaluation::getClarityScore).average().orElse(0);
        double avgRelevance = evaluations.stream().mapToInt(EnglishPracticeEvaluation::getRelevanceScore).average().orElse(0);
        double avgConfidence = evaluations.stream().mapToInt(EnglishPracticeEvaluation::getConfidenceScore).average().orElse(0);
        
        // Find strongest and weakest area
        String[] areas = {"Grammar", "Vocabulary", "Fluency", "Clarity", "Relevance", "Confidence"};
        double[] scores = {avgGrammar, avgVocab, avgFluency, avgClarity, avgRelevance, avgConfidence};
        
        double maxScore = -1;
        double minScore = 101;
        String strongest = null;
        String weakest = null;
        
        for (int i = 0; i < areas.length; i++) {
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                strongest = areas[i];
            }
            if (scores[i] < minScore) {
                minScore = scores[i];
                weakest = areas[i];
            }
        }
        
        return EnglishPracticeProgressResponse.builder()
                .totalSessions(totalSessions)
                .completedSessions(completedSessions)
                .averageOverallScore(avgOverall)
                .averageGrammarScore(avgGrammar)
                .averageVocabularyScore(avgVocab)
                .averageFluencyScore(avgFluency)
                .averageClarityScore(avgClarity)
                .averageRelevanceScore(avgRelevance)
                .averageConfidenceScore(avgConfidence)
                .strongestArea(strongest)
                .weakestArea(weakest)
                .build();
    }

    @Transactional(readOnly = true)
    public List<EnglishPracticeSessionResponse> getSessions() {
        User user = getAuthenticatedUser();
        List<EnglishPracticeSession> sessions = sessionRepository.findByUserOrderByIdDesc(user);
        return sessions.stream().map(this::mapToSessionResponse).toList();
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private EnglishPracticeSessionResponse mapToSessionResponse(EnglishPracticeSession entity) {
        return EnglishPracticeSessionResponse.builder()
                .id(entity.getId())
                .participantOneRole(entity.getParticipantOneRole())
                .participantTwoRole(entity.getParticipantTwoRole())
                .topic(entity.getTopic())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    private EnglishPracticeMessageResponse mapToMessageResponse(EnglishPracticeMessage entity) {
        return EnglishPracticeMessageResponse.builder()
                .id(entity.getId())
                .participantRole(entity.getParticipantRole())
                .messageText(entity.getMessageText())
                .sequenceNumber(entity.getSequenceNumber())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private EnglishPracticeEvaluationResponse mapToEvaluationResponse(EnglishPracticeEvaluation entity) {
        return EnglishPracticeEvaluationResponse.builder()
                .grammarScore(entity.getGrammarScore())
                .vocabularyScore(entity.getVocabularyScore())
                .fluencyScore(entity.getFluencyScore())
                .clarityScore(entity.getClarityScore())
                .relevanceScore(entity.getRelevanceScore())
                .confidenceScore(entity.getConfidenceScore())
                .overallScore(entity.getOverallScore())
                .strengths(entity.getStrengths() != null && !entity.getStrengths().isEmpty() ? List.of(entity.getStrengths().split("\\|\\|")) : List.of())
                .improvements(entity.getImprovements() != null && !entity.getImprovements().isEmpty() ? List.of(entity.getImprovements().split("\\|\\|")) : List.of())
                .feedback(entity.getFeedback())
                .build();
    }
}
