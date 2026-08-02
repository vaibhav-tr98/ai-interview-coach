package com.vaibhav.aiinterviewcoach.interview.service;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;

import com.vaibhav.aiinterviewcoach.interview.dto.InterviewRequest;
import com.vaibhav.aiinterviewcoach.interview.dto.InterviewResponse;
import com.vaibhav.aiinterviewcoach.interview.prompt.PromptBuilder;
import com.vaibhav.aiinterviewcoach.interview.repository.InterviewRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.vaibhav.aiinterviewcoach.entity.User;




@Service
public class InterviewService {

    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;
    private final InterviewRepository interviewRepository;

    public InterviewService(ChatClient.Builder builder,
                            PromptBuilder promptBuilder,
                            InterviewRepository interviewRepository, UserRepository userRepository) {

        this.chatClient = builder.build();
        this.promptBuilder = promptBuilder;
        this.interviewRepository = interviewRepository;
        this.userRepository = userRepository;
    }

    public InterviewResponse startInterview(InterviewRequest request) {

        String prompt = promptBuilder.buildPrompt(
                request.interviewType(),
                request.experienceLevel(),
                request.resume(),
                request.jobDescription(),
                request.projectDescription()
        );

        try {

            String question = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return new InterviewResponse(
                    UUID.randomUUID().toString(),
                    question,
                    request.interviewType()
            );

        } catch (Exception e) {

            Throwable t = e;
            while (t.getCause() != null) {
                t = t.getCause();
            }

            return new InterviewResponse(
                    UUID.randomUUID().toString(),
                    t.toString(),
                    request.interviewType()
            );
        }
    }
    private String getCurrentUserEmail() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return authentication.getName();
    }
    private final UserRepository userRepository;
    private User getCurrentUser() {

        String email = getCurrentUserEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

}