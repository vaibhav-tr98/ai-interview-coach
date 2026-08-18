package com.vaibhav.aiinterviewcoach.interview.service;

import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.interview.dto.InterviewRequest;
import com.vaibhav.aiinterviewcoach.interview.dto.InterviewResponse;
import com.vaibhav.aiinterviewcoach.interview.entity.Interview;
import com.vaibhav.aiinterviewcoach.interview.enums.Difficulty;
import com.vaibhav.aiinterviewcoach.interview.enums.DsaTopic;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewerPersona;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewStatus;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewType;
import com.vaibhav.aiinterviewcoach.interview.prompt.InterviewContext;
import com.vaibhav.aiinterviewcoach.interview.prompt.InterviewState;
import com.vaibhav.aiinterviewcoach.interview.prompt.InterviewTurnContext;
import com.vaibhav.aiinterviewcoach.interview.prompt.PromptBuilder;
import com.vaibhav.aiinterviewcoach.interview.repository.InterviewRepository;
import com.vaibhav.aiinterviewcoach.interview.repository.QuestionAnswerRepository;
import com.vaibhav.aiinterviewcoach.interview.session.InterviewSession;
import com.vaibhav.aiinterviewcoach.interview.session.InterviewSessionRepository;
import com.vaibhav.aiinterviewcoach.interview.session.SessionStatus;
import com.vaibhav.aiinterviewcoach.interview.entity.QuestionAnswer;
import com.vaibhav.aiinterviewcoach.interview.dto.AnswerRequest;
import com.vaibhav.aiinterviewcoach.interview.dto.AnswerResponse;
import com.vaibhav.aiinterviewcoach.interview.dto.EvaluationResponse;
import com.vaibhav.aiinterviewcoach.interview.dto.EvaluationResult;
import com.vaibhav.aiinterviewcoach.interview.dto.SessionResponse;
import com.vaibhav.aiinterviewcoach.interview.entity.AnswerEvaluation;
import com.vaibhav.aiinterviewcoach.interview.repository.AnswerEvaluationRepository;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InterviewService {

    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;
    private final InterviewRepository interviewRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final AnswerEvaluationRepository answerEvaluationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final com.vaibhav.aiinterviewcoach.progress.service.ProgressService progressService;
    private final com.vaibhav.aiinterviewcoach.progress.repository.SkillRepository skillRepository;

    public InterviewService(ChatClient.Builder builder,
                            PromptBuilder promptBuilder,
                            InterviewRepository interviewRepository,
                            InterviewSessionRepository interviewSessionRepository,
                            QuestionAnswerRepository questionAnswerRepository,
                            AnswerEvaluationRepository answerEvaluationRepository,
                            UserRepository userRepository,
                            ObjectMapper objectMapper,
                            com.vaibhav.aiinterviewcoach.progress.service.ProgressService progressService,
                            com.vaibhav.aiinterviewcoach.progress.repository.SkillRepository skillRepository) {

        this.chatClient = builder.build();
        this.promptBuilder = promptBuilder;
        this.interviewRepository = interviewRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.questionAnswerRepository = questionAnswerRepository;
        this.answerEvaluationRepository = answerEvaluationRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.progressService = progressService;
        this.skillRepository = skillRepository;
    }

    public InterviewResponse startInterview(InterviewRequest request) {

        User currentUser = getCurrentUser();
        
        Difficulty difficulty = Difficulty.MEDIUM;
        if (request.dsaDifficulty() != null) {
            try {
                difficulty = Difficulty.valueOf(request.dsaDifficulty().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        
        DsaTopic dsaTopic = null;
        if (request.dsaTopic() != null) {
            try {
                dsaTopic = DsaTopic.valueOf(request.dsaTopic().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        
        InterviewerPersona persona = null;
        if (request.interviewerPersona() != null) {
            try {
                persona = InterviewerPersona.valueOf(request.interviewerPersona().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Interview interview = Interview.builder()
                .title(request.interviewType() + " Interview")
                .type(InterviewType.valueOf(request.interviewType().toUpperCase()))
                .difficulty(difficulty)
                .status(InterviewStatus.CREATED)
                .user(currentUser)
                .role(request.role())
                .experienceLevel(request.experienceLevel())
                .dsaTopic(dsaTopic)
                .resumeText(request.resume())
                .jobDescription(request.jobDescription())
                .projectDescription(request.projectDescription())
                .projectUrl(request.projectUrl())
                .durationMinutes(request.durationMinutes())
                .interviewerPersona(persona)
                .build();

        interview = interviewRepository.save(interview);

        InterviewContext context = buildContext(interview);
        String prompt = promptBuilder.buildInitialQuestionPrompt(context);

        try {

            String jsonResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (jsonResponse != null) {
                jsonResponse = jsonResponse.trim();
                if (jsonResponse.startsWith("```json")) {
                    jsonResponse = jsonResponse.substring(7);
                } else if (jsonResponse.startsWith("```")) {
                    jsonResponse = jsonResponse.substring(3);
                }
                if (jsonResponse.endsWith("```")) {
                    jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
                }
                jsonResponse = jsonResponse.trim();
            }

            com.vaibhav.aiinterviewcoach.interview.dto.InitialInterviewResult result = objectMapper.readValue(jsonResponse, com.vaibhav.aiinterviewcoach.interview.dto.InitialInterviewResult.class);

            if (result.question() == null || result.question().isBlank()) {
                throw new RuntimeException("Missing question in opening generation");
            }

            InterviewSession session = InterviewSession.builder()
                    .interview(interview)
                    .currentQuestion(result.question())
                    .questionNumber(1)
                    .status(SessionStatus.ACTIVE)
                    .build();

            session = interviewSessionRepository.save(session);

            interview.setStatus(InterviewStatus.STARTED);
            interviewRepository.save(interview);

            return new InterviewResponse(
                    interview.getId().toString(),
                    session.getSessionId(),
                    result.interviewerMessage(),
                    result.question(),
                    request.interviewType()
            );

        } catch (Exception e) {
            // Delete the empty interview record if AI fails so we don't leak orphaned CREATED interviews
            interviewRepository.delete(interview);

            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "Failed to generate the interview opening due to an AI service error."
            );
        }
    }

    public AnswerResponse submitAnswer(String sessionId, AnswerRequest request) {
        User currentUser = getCurrentUser();

        InterviewSession session = interviewSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getInterview().getUser().getId().equals(currentUser.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Unauthorized to access this session");
        }

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Session is already completed");
        }

        Interview interview = session.getInterview();
        
        // Enforce duration
        if (interview.getDurationMinutes() != null && session.getCreatedAt() != null) {
            LocalDateTime expiresAt = session.getCreatedAt().plusMinutes(interview.getDurationMinutes());
            if (LocalDateTime.now().isAfter(expiresAt)) {
                completeInterview(session, interview);
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT, "Interview duration has expired."
                );
            }
        }

        String currentQuestion = session.getCurrentQuestion();
        Integer currentQuestionNumber = session.getQuestionNumber();
        InterviewContext context = buildContext(interview);

        QuestionAnswer questionAnswer = QuestionAnswer.builder()
                .session(session)
                .questionNumber(currentQuestionNumber)
                .question(currentQuestion)
                .answer(request.answer())
                .build();

        questionAnswerRepository.save(questionAnswer);

        EvaluationResponse evaluationResponse;
        try {
            List<String> allowedSkills = skillRepository.findAll().stream().map(com.vaibhav.aiinterviewcoach.progress.entity.Skill::getName).toList();
            EvaluationResult evalResult = evaluateAnswer(currentQuestion, request.answer(), context, allowedSkills);
            AnswerEvaluation evaluation = AnswerEvaluation.builder()
                    .questionAnswer(questionAnswer)
                    .score(evalResult.score())
                    .feedback(evalResult.feedback())
                    .strengths(evalResult.strengths())
                    .weaknesses(evalResult.weaknesses())
                    .build();
            answerEvaluationRepository.save(evaluation);
            
            try {
                if (evalResult.skills() != null && !evalResult.skills().isEmpty()) {
                    progressService.processAnswerSkills(currentUser, questionAnswer, evalResult.skills());
                }
            } catch (Exception e) {
                // Log and swallow progress errors to avoid breaking the core interview
                System.err.println("Progress processing failed: " + e.getMessage());
            }

            evaluationResponse = new EvaluationResponse(
                    evaluation.getScore(),
                    evaluation.getFeedback(),
                    evaluation.getStrengths(),
                    evaluation.getWeaknesses(),
                    evalResult.skills()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate answer: " + e.getMessage());
        }

        if (currentQuestionNumber >= 5) {
            completeInterview(session, interview);

            String closingMessage = getClosingMessage(interview.getInterviewerPersona());

            return new AnswerResponse(
                    sessionId,
                    currentQuestionNumber,
                    currentQuestion,
                    request.answer(),
                    evaluationResponse,
                    null,
                    closingMessage,
                    true
            );
        }

        List<InterviewTurnContext> fullHistory = fetchHistory(session.getSessionId());
        List<InterviewTurnContext> history = fullHistory.isEmpty() ? fullHistory : fullHistory.subList(0, fullHistory.size() - 1);

        InterviewState state = InterviewState.builder()
                .nextQuestionNumber(currentQuestionNumber + 1)
                .totalQuestions(5)
                .build();

        String prompt = promptBuilder.buildNextQuestionPrompt(
                context,
                state,
                history,
                currentQuestion,
                request.answer(),
                evaluationResponse
        );

        try {
            String nextQuestion = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            session.setCurrentQuestion(nextQuestion);
            session.setQuestionNumber(currentQuestionNumber + 1);
            interviewSessionRepository.save(session);

            return new AnswerResponse(
                    sessionId,
                    session.getQuestionNumber(),
                    currentQuestion,
                    request.answer(),
                    evaluationResponse,
                    nextQuestion,
                    null,
                    false
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate next question: " + e.getMessage());
        }
    }

    private void completeInterview(InterviewSession session, Interview interview) {
        session.setStatus(SessionStatus.COMPLETED);
        session.setCurrentQuestion(null);
        interviewSessionRepository.save(session);
        
        interview.setStatus(InterviewStatus.COMPLETED);
        interview.setCompletedAt(LocalDateTime.now());
        interviewRepository.save(interview);
    }

    private String getClosingMessage(InterviewerPersona persona) {
        if (persona == null) {
            return "That concludes the interview. Thank you for your time. Your performance report is now available.";
        }
        return switch (persona) {
            case FRIENDLY -> "Great job! That's all the questions I have for today. Thank you so much for your time. Your performance report is now ready.";
            case STRICT -> "We have reached the end of the interview. Thank you for your time. The evaluation report is now generated.";
            case PROFESSIONAL -> "That concludes our interview session. Thank you for your time and answers. Your performance report is available now.";
            case TECHNICAL -> "This concludes the technical assessment. Thank you for going through these scenarios with me. Your detailed report is available.";
            default -> "That concludes the interview. Thank you for your time. Your performance report is now available.";
        };
    }
    
    private List<InterviewTurnContext> fetchHistory(String sessionId) {
        List<QuestionAnswer> qaList = questionAnswerRepository.findBySessionSessionIdOrderByQuestionNumberAsc(sessionId);
        List<InterviewTurnContext> history = new ArrayList<>();
        
        for (QuestionAnswer qa : qaList) {
            Optional<AnswerEvaluation> evalOpt = answerEvaluationRepository.findByQuestionAnswerId(qa.getId());
            if (evalOpt.isPresent()) {
                AnswerEvaluation eval = evalOpt.get();
                history.add(InterviewTurnContext.builder()
                        .questionNumber(qa.getQuestionNumber())
                        .question(qa.getQuestion())
                        .answer(qa.getAnswer())
                        .evaluationScore(eval.getScore())
                        .evaluationFeedback(eval.getFeedback())
                        .evaluationStrengths(eval.getStrengths())
                        .evaluationWeaknesses(eval.getWeaknesses())
                        .build());
            } else {
                history.add(InterviewTurnContext.builder()
                        .questionNumber(qa.getQuestionNumber())
                        .question(qa.getQuestion())
                        .answer(qa.getAnswer())
                        .build());
            }
        }
        return history;
    }
    
    private InterviewContext buildContext(Interview interview) {
        return InterviewContext.builder()
                .interviewType(interview.getType())
                .role(interview.getRole())
                .experienceLevel(interview.getExperienceLevel())
                .difficulty(interview.getDifficulty())
                .dsaTopic(interview.getDsaTopic())
                .resumeText(interview.getResumeText())
                .jobDescription(interview.getJobDescription())
                .projectDescription(interview.getProjectDescription())
                .projectUrl(interview.getProjectUrl())
                .durationMinutes(interview.getDurationMinutes())
                .interviewerPersona(interview.getInterviewerPersona())
                .build();
    }

    public SessionResponse getSession(String sessionId) {
        User currentUser = getCurrentUser();

        InterviewSession session = interviewSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getInterview().getUser().getId().equals(currentUser.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Unauthorized to access this session");
        }

        Interview interview = session.getInterview();
        if (session.getStatus() == SessionStatus.ACTIVE && interview.getDurationMinutes() != null && session.getCreatedAt() != null) {
            LocalDateTime expiresAt = session.getCreatedAt().plusMinutes(interview.getDurationMinutes());
            if (LocalDateTime.now().isAfter(expiresAt)) {
                completeInterview(session, interview);
            }
        }

        return new SessionResponse(
                session.getSessionId(),
                session.getInterview().getId().toString(),
                session.getInterview().getType().name(),
                session.getQuestionNumber(),
                session.getCurrentQuestion(),
                session.getStatus().name()
        );
    }

    public com.vaibhav.aiinterviewcoach.interview.dto.FinalInterviewResponse getFinalResult(String sessionId) {
        User currentUser = getCurrentUser();

        InterviewSession session = interviewSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getInterview().getUser().getId().equals(currentUser.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Unauthorized to access this session");
        }

        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT, "Interview is not completed yet"
            );
        }

        java.util.List<AnswerEvaluation> evaluations = answerEvaluationRepository.findByQuestionAnswerSessionSessionId(sessionId);

        if (evaluations.size() < 5) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Missing evaluations for completed interview");
        }

        double totalScore = 0;
        java.util.List<String> strengths = new java.util.ArrayList<>();
        java.util.List<String> weaknesses = new java.util.ArrayList<>();
        java.util.List<String> recommendations = new java.util.ArrayList<>();

        for (AnswerEvaluation eval : evaluations) {
            totalScore += eval.getScore();
            
            if (eval.getStrengths() != null && !strengths.contains(eval.getStrengths())) {
                strengths.add(eval.getStrengths());
            }
            if (eval.getWeaknesses() != null && !weaknesses.contains(eval.getWeaknesses())) {
                weaknesses.add(eval.getWeaknesses());
                recommendations.add("Consider improving on: " + eval.getWeaknesses());
            }
        }

        int overallScore = (int) Math.round(totalScore / evaluations.size());

        return new com.vaibhav.aiinterviewcoach.interview.dto.FinalInterviewResponse(
                sessionId,
                session.getInterview().getId().toString(),
                session.getInterview().getType().name(),
                5,
                overallScore,
                strengths,
                weaknesses,
                recommendations
        );
    }

    private String getCurrentUserEmail() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return authentication.getName();
    }

    private User getCurrentUser() {

        String email = getCurrentUserEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    public com.vaibhav.aiinterviewcoach.interview.dto.TranscriptResponse getTranscript(String sessionId) {
        User currentUser = getCurrentUser();

        InterviewSession session = interviewSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getInterview().getUser().getId().equals(currentUser.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Unauthorized to access this session");
        }

        List<QuestionAnswer> qaList = questionAnswerRepository.findBySessionSessionIdOrderByQuestionNumberAsc(sessionId);
        List<com.vaibhav.aiinterviewcoach.interview.dto.TranscriptTurn> turns = new ArrayList<>();

        for (QuestionAnswer qa : qaList) {
            Optional<AnswerEvaluation> evalOpt = answerEvaluationRepository.findByQuestionAnswerId(qa.getId());
            EvaluationResponse evalResponse = null;
            if (evalOpt.isPresent()) {
                AnswerEvaluation eval = evalOpt.get();
                evalResponse = new EvaluationResponse(
                        eval.getScore(),
                        eval.getFeedback(),
                        eval.getStrengths(),
                        eval.getWeaknesses(),
                        null
                );
            }
            turns.add(new com.vaibhav.aiinterviewcoach.interview.dto.TranscriptTurn(
                    qa.getQuestionNumber(),
                    qa.getQuestion(),
                    qa.getAnswer(),
                    evalResponse
            ));
        }

        return new com.vaibhav.aiinterviewcoach.interview.dto.TranscriptResponse(
                sessionId,
                session.getInterview().getId().toString(),
                session.getInterview().getType().name(),
                turns
        );
    }

    public EvaluationResult evaluateAnswer(String question, String answer, InterviewContext context, List<String> allowedSkills) {
        String prompt = promptBuilder.buildAnswerEvaluationPrompt(question, answer, context, allowedSkills);

        try {
            String jsonResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (jsonResponse != null) {
                jsonResponse = jsonResponse.trim();
                if (jsonResponse.startsWith("```json")) {
                    jsonResponse = jsonResponse.substring(7);
                } else if (jsonResponse.startsWith("```")) {
                    jsonResponse = jsonResponse.substring(3);
                }
                if (jsonResponse.endsWith("```")) {
                    jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
                }
                jsonResponse = jsonResponse.trim();
            }

            EvaluationResult result = objectMapper.readValue(jsonResponse, EvaluationResult.class);

            if (result.score() == null || result.score() < 0 || result.score() > 100) {
                throw new RuntimeException("Invalid score in evaluation: " + result.score());
            }
            if (result.feedback() == null || result.feedback().isBlank() ||
                result.strengths() == null || result.strengths().isBlank() ||
                result.weaknesses() == null || result.weaknesses().isBlank()) {
                throw new RuntimeException("Missing required text fields in evaluation");
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("AI evaluation failed to produce a valid result.");
        }
    }
}
