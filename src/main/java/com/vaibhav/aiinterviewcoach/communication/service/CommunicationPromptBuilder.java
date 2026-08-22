package com.vaibhav.aiinterviewcoach.communication.service;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CommunicationPromptBuilder {

    public String buildAssessmentPrompt(List<String> questions, List<String> answers) {
        StringBuilder qaPairText = new StringBuilder();
        for (int i = 0; i < questions.size(); i++) {
            qaPairText.append("Question ").append(i + 1).append(": ").append(questions.get(i)).append("\n");
            qaPairText.append("Answer ").append(i + 1).append(": ").append(answers.get(i)).append("\n\n");
        }

        return """
            Analyze the following interview questions and the candidate's answers to evaluate their communication and fluency.
            
            Q&A Pairs:
            %s
            
            Return the output in STRICT JSON format exactly matching this structure:
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
            
            Rules:
            1. DO NOT use markdown formatting (no ```json).
            2. All scores must be integers between 0 and 100.
            3. fillerWordCount and repetitionCount must be non-negative integers. Look for words like 'um', 'uh', 'like', 'you know', etc., or repeated phrases.
            4. Strengths, weaknesses, and recommendations must be lists of strings. Max 3 items each.
            5. ONLY return valid JSON. Do not include any explanations outside the JSON object.
            """.formatted(qaPairText.toString());
    }
}
