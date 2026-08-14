package com.vaibhav.aiinterviewcoach.interview.service;

import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.interview.dto.InterviewRequest;
import com.vaibhav.aiinterviewcoach.interview.dto.InterviewResponse;
import com.vaibhav.aiinterviewcoach.interview.entity.Interview;
import com.vaibhav.aiinterviewcoach.interview.enums.Difficulty;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewStatus;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewType;
import com.vaibhav.aiinterviewcoach.interview.prompt.PromptBuilder;
import com.vaibhav.aiinterviewcoach.interview.repository.InterviewRepository;
import com.vaibhav.aiinterviewcoach.interview.repository.QuestionAnswerRepository;
import com.vaibhav.aiinterviewcoach.interview.session.InterviewSession;
import com.vaibhav.aiinterviewcoach.interview.session.InterviewSessionRepository;
import com.vaibhav.aiinterviewcoach.interview.session.SessionStatus;
import com.vaibhav.aiinterviewcoach.interview.entity.QuestionAnswer;
import com.vaibhav.aiinterviewcoach.interview.dto.AnswerRequest;
import com.vaibhav.aiinterviewcoach.interview.dto.AnswerResponse;
import com.vaibhav.aiinterviewcoach.interview.dto.EvaluationResult;
import com.vaibhav.aiinterviewcoach.interview.dto.SessionResponse;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class InterviewService {

    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;
    private final InterviewRepository interviewRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public InterviewService(ChatClient.Builder builder,
                            PromptBuilder promptBuilder,
                            InterviewRepository interviewRepository,
                            InterviewSessionRepository interviewSessionRepository,
                            QuestionAnswerRepository questionAnswerRepository,
                            UserRepository userRepository,
                            ObjectMapper objectMapper) {

        this.chatClient = builder.build();
        this.promptBuilder = promptBuilder;
        this.interviewRepository = interviewRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.questionAnswerRepository = questionAnswerRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public InterviewResponse startInterview(InterviewRequest request) {

        // Get the currently logged-in user
        User currentUser = getCurrentUser();

        // Create Interview entity
        Interview interview = Interview.builder()
                .title(request.interviewType() + " Interview")
                .type(InterviewType.valueOf(request.interviewType().toUpperCase()))
                .difficulty(Difficulty.MEDIUM)
                .status(InterviewStatus.CREATED)
                .user(currentUser)
                .build();

        // Save interview in database
        interview = interviewRepository.save(interview);

        // Build AI prompt
        String prompt = promptBuilder.buildPrompt(
                request.interviewType(),
                request.experienceLevel(),
                request.resume(),
                request.jobDescription(),
                request.projectDescription()
        );

        try {

            // Generate AI question
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

        String currentQuestion = session.getCurrentQuestion();
        Integer currentQuestionNumber = session.getQuestionNumber();

        QuestionAnswer questionAnswer = QuestionAnswer.builder()
                .session(session)
                .questionNumber(currentQuestionNumber)
                .question(currentQuestion)
                .answer(request.answer())
                .build();

        questionAnswerRepository.save(questionAnswer);

        if (currentQuestionNumber >= 5) {
            session.setStatus(SessionStatus.COMPLETED);
            session.setCurrentQuestion(null);
            interviewSessionRepository.save(session);

            Interview interview = session.getInterview();
            interview.setStatus(InterviewStatus.COMPLETED);
            interviewRepository.save(interview);

            return new AnswerResponse(
                    sessionId,
                    currentQuestionNumber,
                    currentQuestion,
                    request.answer(),
                    null,
                    true
            );
        }

        String prompt = promptBuilder.buildNextQuestionPrompt(
                session.getInterview().getType().name(),
                currentQuestionNumber + 1,
                currentQuestion,
                request.answer()
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
                    nextQuestion,
                    false
            );
        } catch (Exception e) {
            // Keep the answer persisted, but return error response
            throw new RuntimeException("Failed to generate next question: " + e.getMessage());
        }
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

    public EvaluationResult evaluateAnswer(String question, String answer, String interviewType) {
        String prompt = promptBuilder.buildAnswerEvaluationPrompt(question, answer, interviewType);

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
