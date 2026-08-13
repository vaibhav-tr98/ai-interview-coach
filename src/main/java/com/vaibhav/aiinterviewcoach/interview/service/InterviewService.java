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
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
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

    public InterviewService(ChatClient.Builder builder,
                            PromptBuilder promptBuilder,
                            InterviewRepository interviewRepository,
                            InterviewSessionRepository interviewSessionRepository,
                            QuestionAnswerRepository questionAnswerRepository,
                            UserRepository userRepository) {

        this.chatClient = builder.build();
        this.promptBuilder = promptBuilder;
        this.interviewRepository = interviewRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.questionAnswerRepository = questionAnswerRepository;
        this.userRepository = userRepository;
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
                    nextQuestion
            );
        } catch (Exception e) {
            // Keep the answer persisted, but return error response
            throw new RuntimeException("Failed to generate next question: " + e.getMessage());
        }
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
}
