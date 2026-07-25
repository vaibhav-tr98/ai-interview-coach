package com.vaibhav.aiinterviewcoach.interview.service;

import com.vaibhav.aiinterviewcoach.interview.dto.InterviewRequest;
import com.vaibhav.aiinterviewcoach.interview.dto.InterviewResponse;
import com.vaibhav.aiinterviewcoach.interview.prompt.PromptBuilder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InterviewService {

    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;

    public InterviewService(ChatClient.Builder builder,
                            PromptBuilder promptBuilder) {
        this.chatClient = builder.build();
        this.promptBuilder = promptBuilder;
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
            e.printStackTrace();

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