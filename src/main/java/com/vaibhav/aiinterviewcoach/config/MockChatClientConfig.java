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
                
                if (req.contains("fillerWordCount")) {
                    responseStr = """
                        {
                          "overallScore": 85,
                          "clarityScore": 80,
                          "relevanceScore": 90,
                          "completenessScore": 85,
                          "concisenessScore": 75,
                          "vocabularyScore": 80,
                          "confidenceScore": 85,
                          "fillerWordCount": 5,
                          "repetitionCount": 2,
                          "strengths": ["Clear pronunciation", "Good use of technical vocabulary"],
                          "weaknesses": ["Uses filler words frequently", "Some answers are slightly overly long"],
                          "recommendations": ["Practice pausing instead of using filler words", "Focus on the STAR method to stay concise"]
                        }
                    """;
                } else if (req.contains("InitialInterviewResult") || req.contains("interviewerMessage") || req.contains("deep project-based interview") || req.contains("deep resume-based interview")) {
                    responseStr = """
                        {
                            "interviewerMessage": "Hello, let's start the deep interview.",
                            "question": "Could you walk me through the architecture of your project?"
                        }
                    """;
                } else if (req.contains("technical depth/ownership")) {
                    responseStr = """
                        {
                          "evaluation": {
                            "score": 85,
                            "feedback": "Good architecture overview.",
                            "strengths": "Clear explanation of components.",
                            "weaknesses": "Lacks some depth on database indexing.",
                            "technicalCorrectnessScore": 80,
                            "depthScore": 70,
                            "projectOwnershipScore": 90,
                            "consistencySignal": true,
                            "communicationScore": 85,
                            "confidenceScore": 80,
                            "unsupportedClaims": "None"
                          },
                          "nextQuestion": "How did you optimize your PostgreSQL queries?",
                          "closingMessage": "Thank you, that's all."
                        }
                    """;
                } else if (req.contains("final deep analysis")) {
                    responseStr = """
                        {
                          "overallScore": 85,
                          "technicalCorrectnessScore": 80,
                          "depthScore": 75,
                          "projectOwnershipScore": 90,
                          "consistencySignal": true,
                          "communicationScore": 85,
                          "confidenceScore": 80,
                          "strengths": ["Strong architectural knowledge"],
                          "weaknesses": ["Database optimization"],
                          "unsupportedClaims": "None",
                          "recommendations": ["Study query execution plans"],
                          "interviewSummary": "Solid candidate with strong ownership."
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
                                    {"skill": "SPRING_SECURITY", "score": 40, "relevance": 90},
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
                } else if (req.contains("experienceLevel")) {
                    responseStr = """
                        {
                          "summary": "Experienced Java Developer.",
                          "experienceLevel": "MID_LEVEL",
                          "skills": [
                            {"skill": "JAVA", "confidence": 95},
                            {"skill": "SPRING_BOOT", "confidence": 90},
                            {"skill": "SPRING_SECURITY", "confidence": 85},
                            {"skill": "POSTGRESQL", "confidence": 80}
                          ]
                        }
                    """;
                } else if (req.contains("requiredSkills")) {
                    responseStr = """
                        {
                          "company": "Tech Corp",
                          "role": "Java Backend Developer",
                          "seniority": "MID_LEVEL",
                          "summary": "Looking for a strong Java developer.",
                          "requiredSkills": [
                            {"skill": "JAVA", "importance": 100},
                            {"skill": "SPRING_BOOT", "importance": 90},
                            {"skill": "SPRING_SECURITY", "importance": 80}
                          ],
                          "preferredSkills": [
                            {"skill": "POSTGRESQL", "importance": 60}
                          ]
                        }
                    """;
                } else if (req.contains("claims")) {
                    responseStr = """
                        {
                          "claims": [
                            {
                              "claimText": "Built a microservices architecture handling 10k RPS",
                              "status": "NEEDS_VERIFICATION",
                              "verificationQuestions": "How did you measure the 10k RPS?"
                            }
                          ]
                        }
                    """;
                } else if (req.contains("nextQuestion")) {
                    responseStr = "What is Spring Boot?";
                } else if (req.contains("targetRole") && req.contains("dayNumber")) {
                    responseStr = """
                        {
                          "targetRole": "Java Backend Developer",
                          "days": [
                            {
                              "dayNumber": 1,
                              "skill": "SPRING_SECURITY",
                              "topic": "JWT Authentication",
                              "description": "Study JWT authentication flow"
                            },
                            {
                              "dayNumber": 2,
                              "skill": "POSTGRESQL",
                              "topic": "Indexes",
                              "description": "Learn DB indexes"
                            }
                          ]
                        }
                    """;
                } else if (req.contains("Generate a new coding problem")) {
                    responseStr = """
                        {
                          "title": "Two Sum",
                          "description": "Find two numbers that add up to a target.",
                          "constraints": "Array size > 2",
                          "examples": "input: [1, 2], target: 3, output: [0, 1]",
                          "expectedInputFormat": "int[] nums, int target",
                          "expectedOutputFormat": "int[]"
                        }
                    """;
                } else if (req.contains("Give a brief hint for solving this problem")) {
                    responseStr = "Consider using a HashMap to store the numbers you have seen so far.";
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
