package com.vaibhav.aiinterviewcoach.planner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaibhav.aiinterviewcoach.planner.dto.AiStudyPlanResponse;
import com.vaibhav.aiinterviewcoach.planner.prompt.PlannerPromptBuilder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class StudyPlanAiService {

    private final ChatClient chatClient;
    private final PlannerPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    public StudyPlanAiService(ChatClient.Builder builder, PlannerPromptBuilder promptBuilder, ObjectMapper objectMapper) {
        this.chatClient = builder.build();
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
    }

    public AiStudyPlanResponse generateStudyPlan(String targetRole, String weaknessesStr, String allowedSkillsStr) {
        String prompt = promptBuilder.buildStudyPlanPrompt(targetRole, weaknessesStr, allowedSkillsStr);

        String jsonResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        try {
            // Trim markdown block if the model outputs it despite instructions
            if (jsonResponse.startsWith("```json")) {
                jsonResponse = jsonResponse.substring(7);
                if (jsonResponse.endsWith("```")) {
                    jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
                }
            } else if (jsonResponse.startsWith("```")) {
                jsonResponse = jsonResponse.substring(3);
                if (jsonResponse.endsWith("```")) {
                    jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
                }
            }
            return objectMapper.readValue(jsonResponse, AiStudyPlanResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response into AiStudyPlanResponse: " + e.getMessage(), e);
        }
    }
}
