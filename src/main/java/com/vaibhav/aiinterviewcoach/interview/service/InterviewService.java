package com.vaibhav.aiinterviewcoach.interview.service;

import com.vaibhav.aiinterviewcoach.interview.dto.InterviewRequest;
import com.vaibhav.aiinterviewcoach.interview.dto.InterviewResponse;
import com.vaibhav.aiinterviewcoach.interview.prompt.PromptBuilder;
import com.vaibhav.aiinterviewcoach.interview.repository.InterviewRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InterviewService {

    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;
    private final InterviewRepository interviewRepository;

    public InterviewService(ChatClient.Builder builder,
                            PromptBuilder promptBuilder,
                            InterviewRepository interviewRepository) {

        this.chatClient = builder.build();
        this.promptBuilder = promptBuilder;
        this.interviewRepository = interviewRepository;
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
}