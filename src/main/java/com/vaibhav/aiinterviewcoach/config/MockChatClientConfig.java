package com.vaibhav.aiinterviewcoach.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test-m7")
public class MockChatClientConfig {

    @Bean
    @Primary
    public ChatClient.Builder chatClientBuilder() {
        return ChatClient.builder(new org.springframework.ai.chat.model.ChatModel() {
            @Override
            public org.springframework.ai.chat.model.ChatResponse call(org.springframework.ai.chat.prompt.Prompt prompt) {
                String req = prompt.getContents();
                String responseStr = "";
                
                if (req.contains("InitialInterviewResult") || req.contains("interviewerMessage")) {
                    responseStr = """
                        {
                            "interviewerMessage": "Hello, let's start the Java interview.",
                            "question": "What is polymorphism in Java?"
                        }
                    """;
                } else if (req.contains("EvaluationResult") || req.contains("strengths")) {
                    // It's the evaluateAnswer call
                    // We want to return a good score for the first call, and a bad score for the second call.
                    if (req.contains("snake")) {
                        responseStr = """
                            {
                                "score": 40,
                                "feedback": "Poor answer.",
                                "strengths": "None.",
                                "weaknesses": "Everything.",
                                "skills": [
                                    {"skill": "JAVA", "score": 40, "relevance": 90},
                                    {"skill": "SPRING_BOOT", "score": 40, "relevance": 90}
                                ]
                            }
                        """;
                    } else {
                        responseStr = """
                            {
                                "score": 90,
                                "feedback": "Great answer.",
                                "strengths": "Good understanding.",
                                "weaknesses": "None.",
                                "skills": [
                                    {"skill": "JAVA", "score": 90, "relevance": 90},
                                    {"skill": "SPRING_BOOT", "score": 90, "relevance": 90}
                                ]
                            }
                        """;
                    }
                } else if (req.contains("nextQuestion")) {
                    responseStr = "What is Spring Boot?";
                }
                
                return new org.springframework.ai.chat.model.ChatResponse(java.util.List.of(
                    new org.springframework.ai.chat.model.Generation(new org.springframework.ai.chat.messages.AssistantMessage(responseStr))
                ));
            }
            
            @Override
            public org.springframework.ai.chat.prompt.ChatOptions getDefaultOptions() {
                return null;
            }
        });
    }
}
