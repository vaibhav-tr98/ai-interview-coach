package com.vaibhav.aiinterviewcoach.deepinterview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaibhav.aiinterviewcoach.deepinterview.dto.*;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.intelligence.entity.Resume;
import com.vaibhav.aiinterviewcoach.intelligence.repository.ResumeRepository;
import com.vaibhav.aiinterviewcoach.interview.entity.AnswerEvaluation;
import com.vaibhav.aiinterviewcoach.interview.entity.Interview;
import com.vaibhav.aiinterviewcoach.interview.entity.QuestionAnswer;
import com.vaibhav.aiinterviewcoach.interview.enums.Difficulty;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewStatus;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewType;
import com.vaibhav.aiinterviewcoach.interview.repository.AnswerEvaluationRepository;
import com.vaibhav.aiinterviewcoach.interview.repository.InterviewRepository;
import com.vaibhav.aiinterviewcoach.interview.repository.QuestionAnswerRepository;
import com.vaibhav.aiinterviewcoach.interview.session.InterviewSession;
import com.vaibhav.aiinterviewcoach.interview.session.InterviewSessionRepository;
import com.vaibhav.aiinterviewcoach.interview.session.SessionStatus;
import com.vaibhav.aiinterviewcoach.project.entity.Project;
import com.vaibhav.aiinterviewcoach.project.repository.ProjectRepository;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeepInterviewService {

    private final ChatClient chatClient;
    private final InterviewRepository interviewRepository;
    private final InterviewSessionRepository sessionRepository;
    private final QuestionAnswerRepository qaRepository;
    private final AnswerEvaluationRepository evaluationRepository;
    private final ProjectRepository projectRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public DeepInterviewService(ChatClient.Builder builder,
                                InterviewRepository interviewRepository,
                                InterviewSessionRepository sessionRepository,
                                QuestionAnswerRepository qaRepository,
                                AnswerEvaluationRepository evaluationRepository,
                                ProjectRepository projectRepository,
                                ResumeRepository resumeRepository,
                                UserRepository userRepository,
                                ObjectMapper objectMapper) {
        this.chatClient = builder.build();
        this.interviewRepository = interviewRepository;
        this.sessionRepository = sessionRepository;
        this.qaRepository = qaRepository;
        this.evaluationRepository = evaluationRepository;
        this.projectRepository = projectRepository;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public DeepInterviewStartResponse startProjectInterview(Long projectId) {
        User user = getCurrentUser();
        Project project = projectRepository.findByIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found or no access"));

        Interview interview = Interview.builder()
                .title("Deep Project Interview: " + project.getName())
                .type(InterviewType.PROJECT)
                .difficulty(Difficulty.HARD)
                .status(InterviewStatus.CREATED)
                .user(user)
                .projectDescription("Name: " + project.getName() + "\nDesc: " + project.getDescription() + "\nTech: " + project.getTechStack())
                .projectUrl(project.getProjectUrl())
                .build();
        
        interview = interviewRepository.save(interview);

        String prompt = "You are a senior technical interviewer conducting a deep project-based interview.\n"
                + "The candidate claims to have worked on the following project:\n"
                + "Name: " + project.getName() + "\n"
                + "Description: " + project.getDescription() + "\n"
                + "Tech Stack: " + project.getTechStack() + "\n\n"
                + "Generate an opening message and the FIRST technical question. Probe architecture or their specific role.\n"
                + "Return ONLY a JSON object exactly matching this format: {\"interviewerMessage\":\"...\",\"question\":\"...\"}";

        com.vaibhav.aiinterviewcoach.interview.dto.InitialInterviewResult result = executeAiPrompt(prompt, com.vaibhav.aiinterviewcoach.interview.dto.InitialInterviewResult.class);

        return startSession(interview, result.interviewerMessage(), result.question());
    }

    public DeepInterviewStartResponse startResumeInterview(Long resumeId) {
        User user = getCurrentUser();
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your resume");
        }

        Interview interview = Interview.builder()
                .title("Deep Resume Interview: " + resume.getTitle())
                .type(InterviewType.RESUME)
                .difficulty(Difficulty.HARD)
                .status(InterviewStatus.CREATED)
                .user(user)
                .resumeText(resume.getRawText())
                .build();
        
        interview = interviewRepository.save(interview);

        String prompt = "You are a senior technical interviewer conducting a deep resume-based interview.\n"
                + "Candidate Resume:\n" + resume.getRawText() + "\n\n"
                + "Generate an opening message and the FIRST technical question targeting a specific claim from their experience.\n"
                + "Return ONLY a JSON object exactly matching this format: {\"interviewerMessage\":\"...\",\"question\":\"...\"}";

        com.vaibhav.aiinterviewcoach.interview.dto.InitialInterviewResult result = executeAiPrompt(prompt, com.vaibhav.aiinterviewcoach.interview.dto.InitialInterviewResult.class);

        return startSession(interview, result.interviewerMessage(), result.question());
    }

    private DeepInterviewStartResponse startSession(Interview interview, String message, String question) {
        InterviewSession session = InterviewSession.builder()
                .interview(interview)
                .currentQuestion(question)
                .questionNumber(1)
                .status(SessionStatus.ACTIVE)
                .build();
        session = sessionRepository.save(session);
        
        interview.setStatus(InterviewStatus.STARTED);
        interviewRepository.save(interview);

        return new DeepInterviewStartResponse(
                interview.getId().toString(),
                session.getSessionId(),
                message,
                question,
                interview.getType().name()
        );
    }

    public DeepInterviewAnswerResponse submitAnswer(String sessionId, DeepInterviewAnswerRequest request) {
        User user = getCurrentUser();
        InterviewSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getInterview().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
        }

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session already completed");
        }

        int currentQNum = session.getQuestionNumber();
        String currentQuestion = session.getCurrentQuestion();

        QuestionAnswer qa = QuestionAnswer.builder()
                .session(session)
                .questionNumber(currentQNum)
                .question(currentQuestion)
                .answer(request.answer())
                .build();
        qa = qaRepository.save(qa);

        // Fetch history
        List<QuestionAnswer> history = qaRepository.findBySessionSessionIdOrderByQuestionNumberAsc(sessionId);
        StringBuilder historyContext = new StringBuilder();
        for (QuestionAnswer pastQa : history) {
            if (pastQa.getId().equals(qa.getId())) continue;
            historyContext.append("Q").append(pastQa.getQuestionNumber()).append(": ").append(pastQa.getQuestion()).append("\n");
            historyContext.append("A").append(pastQa.getQuestionNumber()).append(": ").append(pastQa.getAnswer()).append("\n\n");
        }

        String contextStr = session.getInterview().getType() == InterviewType.PROJECT 
            ? "Project Context:\n" + session.getInterview().getProjectDescription()
            : "Resume Context:\n" + session.getInterview().getResumeText();

        boolean isFinal = currentQNum >= 5;

        String prompt = "You are a senior interviewer evaluating a candidate's answer and determining technical depth/ownership.\n"
                + contextStr + "\n\n"
                + "Interview History:\n" + historyContext.toString() + "\n"
                + "Current Question: " + currentQuestion + "\n"
                + "Candidate Answer: " + request.answer() + "\n\n"
                + "Evaluate this answer. Then, if this is not the final question (" + isFinal + "), generate an adaptive follow-up question. "
                + "The follow-up MUST directly probe the weaknesses or missing technical depth in their current answer.\n\n"
                + "Return ONLY a JSON object matching this schema:\n"
                + "{\n"
                + "  \"evaluation\": {\n"
                + "    \"score\": 85, \"feedback\": \"...\", \"strengths\": \"...\", \"weaknesses\": \"...\",\n"
                + "    \"technicalCorrectnessScore\": 80, \"depthScore\": 70, \"projectOwnershipScore\": 90,\n"
                + "    \"consistencySignal\": true, \"communicationScore\": 85, \"confidenceScore\": 80,\n"
                + "    \"unsupportedClaims\": \"None\"\n"
                + "  },\n"
                + "  \"nextQuestion\": " + (isFinal ? "null" : "\"...\"") + ",\n"
                + "  \"closingMessage\": " + (isFinal ? "\"...\"" : "null") + "\n"
                + "}";

        DeepAnswerAIResponse result = executeAiPrompt(prompt, DeepAnswerAIResponse.class);

        AnswerEvaluation eval = AnswerEvaluation.builder()
                .questionAnswer(qa)
                .score(result.evaluation().score())
                .feedback(result.evaluation().feedback())
                .strengths(result.evaluation().strengths())
                .weaknesses(result.evaluation().weaknesses())
                // Set M13 fields manually
                .build();
                
        eval.setTechnicalCorrectnessScore(result.evaluation().technicalCorrectnessScore());
        eval.setDepthScore(result.evaluation().depthScore());
        eval.setProjectOwnershipScore(result.evaluation().projectOwnershipScore());
        eval.setConsistencySignal(result.evaluation().consistencySignal());
        eval.setCommunicationScore(result.evaluation().communicationScore());
        eval.setConfidenceScore(result.evaluation().confidenceScore());
        eval.setUnsupportedClaims(result.evaluation().unsupportedClaims());
        
        evaluationRepository.save(eval);

        if (isFinal) {
            session.setStatus(SessionStatus.COMPLETED);
            session.setCurrentQuestion(null);
            sessionRepository.save(session);
            
            Interview interview = session.getInterview();
            interview.setStatus(InterviewStatus.COMPLETED);
            interview.setCompletedAt(LocalDateTime.now());
            interviewRepository.save(interview);
            
            return new DeepInterviewAnswerResponse(
                    sessionId, currentQNum, currentQuestion, request.answer(),
                    result.evaluation(), null, result.closingMessage() != null ? result.closingMessage() : "Interview complete.", true
            );
        } else {
            session.setCurrentQuestion(result.nextQuestion());
            session.setQuestionNumber(currentQNum + 1);
            sessionRepository.save(session);
            
            return new DeepInterviewAnswerResponse(
                    sessionId, session.getQuestionNumber(), currentQuestion, request.answer(),
                    result.evaluation(), result.nextQuestion(), null, false
            );
        }
    }

    public DeepInterviewResultResponse getFinalResult(String sessionId) {
        User user = getCurrentUser();
        InterviewSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getInterview().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
        }

        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Interview not completed");
        }

        List<QuestionAnswer> history = qaRepository.findBySessionSessionIdOrderByQuestionNumberAsc(sessionId);
        
        StringBuilder contextBuilder = new StringBuilder();
        for (QuestionAnswer qa : history) {
            contextBuilder.append("Q: ").append(qa.getQuestion()).append("\n");
            contextBuilder.append("A: ").append(qa.getAnswer()).append("\n");
            AnswerEvaluation eval = evaluationRepository.findByQuestionAnswerId(qa.getId()).orElse(null);
            if (eval != null) {
                contextBuilder.append("[Score: ").append(eval.getScore()).append(", Ownership: ").append(eval.getProjectOwnershipScore()).append("]\n\n");
            }
        }

        String prompt = "You are a senior technical interviewer providing a final deep analysis of a completed interview.\n"
                + "Here is the full Q&A history:\n" + contextBuilder.toString() + "\n\n"
                + "Analyze the overall performance, technical depth, and project ownership.\n"
                + "Return ONLY a JSON object matching this schema:\n"
                + "{\n"
                + "  \"overallScore\": 85,\n"
                + "  \"technicalCorrectnessScore\": 80,\n"
                + "  \"depthScore\": 75,\n"
                + "  \"projectOwnershipScore\": 90,\n"
                + "  \"consistencySignal\": true,\n"
                + "  \"communicationScore\": 85,\n"
                + "  \"confidenceScore\": 80,\n"
                + "  \"strengths\": [\"...\", \"...\"],\n"
                + "  \"weaknesses\": [\"...\", \"...\"],\n"
                + "  \"unsupportedClaims\": \"None found\",\n"
                + "  \"recommendations\": [\"...\", \"...\"],\n"
                + "  \"interviewSummary\": \"...\"\n"
                + "}";

        DeepAnalysisAIResponse aiResponse = executeAiPrompt(prompt, DeepAnalysisAIResponse.class);

        return new DeepInterviewResultResponse(
                sessionId,
                session.getInterview().getId().toString(),
                session.getInterview().getType().name(),
                history.size(),
                aiResponse.overallScore(),
                aiResponse.technicalCorrectnessScore(),
                aiResponse.depthScore(),
                aiResponse.projectOwnershipScore(),
                aiResponse.consistencySignal(),
                aiResponse.communicationScore(),
                aiResponse.confidenceScore(),
                aiResponse.strengths(),
                aiResponse.weaknesses(),
                aiResponse.unsupportedClaims(),
                aiResponse.recommendations(),
                aiResponse.interviewSummary()
        );
    }

    private <T> T executeAiPrompt(String prompt, Class<T> clazz) {
        try {
            String jsonResponse = chatClient.prompt().user(prompt).call().content();
            if (jsonResponse != null) {
                jsonResponse = jsonResponse.trim();
                if (jsonResponse.startsWith("```json")) jsonResponse = jsonResponse.substring(7);
                else if (jsonResponse.startsWith("```")) jsonResponse = jsonResponse.substring(3);
                if (jsonResponse.endsWith("```")) jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
                jsonResponse = jsonResponse.trim();
            }
            return objectMapper.readValue(jsonResponse, clazz);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI call failed: " + e.getMessage());
        }
    }
}
