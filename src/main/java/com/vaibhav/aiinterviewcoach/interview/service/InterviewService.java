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

    public InterviewService(ChatClient.Builder builder,
                            PromptBuilder promptBuilder,
                            InterviewRepository interviewRepository,
                            InterviewSessionRepository interviewSessionRepository,
                            QuestionAnswerRepository questionAnswerRepository,
                            AnswerEvaluationRepository answerEvaluationRepository,
                            UserRepository userRepository,
                            ObjectMapper objectMapper) {

        this.chatClient = builder.build();
        this.promptBuilder = promptBuilder;
        this.interviewRepository = interviewRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.questionAnswerRepository = questionAnswerRepository;
        this.answerEvaluationRepository = answerEvaluationRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
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

            String question = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            InterviewSession session = InterviewSession.builder()
                    .interview(interview)
                    .currentQuestion(question)
                    .questionNumber(1)
                    .status(SessionStatus.ACTIVE)
                    .build();

            session = interviewSessionRepository.save(session);

            interview.setStatus(InterviewStatus.STARTED);
            interviewRepository.save(interview);

            return new InterviewResponse(
                    interview.getId().toString(),
                    session.getSessionId(),
                    question,
                    request.interviewType()
            );

        } catch (Exception e) {

            Throwable t = e;
            while (t.getCause() != null) {
                t = t.getCause();
            }

            return new InterviewResponse(
                    interview.getId().toString(),
                    null,
                    t.toString(),
                    request.interviewType()
            );
        }
    }

    public AnswerResponse submitAnswer(String sessionId, AnswerRequest request) {
        User currentUser = getCurrentUser();

        InterviewSession session = interviewSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getInterview().getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized to access this session");
        }

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new RuntimeException("Session is already completed");
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
            EvaluationResult evalResult = evaluateAnswer(currentQuestion, request.answer(), context);
            AnswerEvaluation evaluation = AnswerEvaluation.builder()
                    .questionAnswer(questionAnswer)
                    .score(evalResult.score())
                    .feedback(evalResult.feedback())
                    .strengths(evalResult.strengths())
                    .weaknesses(evalResult.weaknesses())
                    .build();
            answerEvaluationRepository.save(evaluation);

            evaluationResponse = new EvaluationResponse(
                    evaluation.getScore(),
                    evaluation.getFeedback(),
                    evaluation.getStrengths(),
                    evaluation.getWeaknesses()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate answer: " + e.getMessage());
        }

        if (currentQuestionNumber >= 5) {
            completeInterview(session, interview);

            return new AnswerResponse(
                    sessionId,
                    currentQuestionNumber,
                    currentQuestion,
                    request.answer(),
                    evaluationResponse,
                    null,
                    true
            );
        }

        List<InterviewTurnContext> history = fetchHistory(session.getSessionId());

        InterviewState state = InterviewState.builder()
                .nextQuestionNumber(currentQuestionNumber + 1)
                .totalQuestions(5)
                .build();

        String prompt = promptBuilder.buildNextQuestionPrompt(
                context,
                state,
                history
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
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getInterview().getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized to access this session");
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
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getInterview().getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized to access this session");
        }

        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT, "Interview is not completed yet"
            );
        }

        java.util.List<AnswerEvaluation> evaluations = answerEvaluationRepository.findByQuestionAnswerSessionSessionId(sessionId);

        if (evaluations.size() < 5) {
            throw new RuntimeException("Missing evaluations for completed interview");
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

    public EvaluationResult evaluateAnswer(String question, String answer, InterviewContext context) {
        String prompt = promptBuilder.buildAnswerEvaluationPrompt(question, answer, context);

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
            throw new RuntimeException("AI evaluation failed to produce a valid result.");
        }
    }
}
