package com.vaibhav.aiinterviewcoach.english.service;

import com.vaibhav.aiinterviewcoach.english.entity.EnglishPracticeMessage;
import com.vaibhav.aiinterviewcoach.english.entity.EnglishPracticeSession;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EnglishPracticePromptBuilder {

    public String buildEvaluationPrompt(EnglishPracticeSession session, List<EnglishPracticeMessage> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert English communication evaluator.\n");
        sb.append("Evaluate the following two-person conversation.\n");
        sb.append("Topic: ").append(session.getTopic()).append("\n");
        sb.append("Roles: ").append(session.getParticipantOneRole()).append(" and ").append(session.getParticipantTwoRole()).append("\n\n");
        sb.append("Conversation History:\n");
        for (EnglishPracticeMessage msg : messages) {
            sb.append(msg.getParticipantRole()).append(": ").append(msg.getMessageText()).append("\n");
        }
        sb.append("\nEvaluate the conversation based on Grammar, Vocabulary, Fluency, Clarity, Relevance, and Confidence.\n");
        sb.append("Provide scores out of 100 for each, and an overallScore.\n");
        sb.append("Provide a list of strengths and improvements, and a general feedback paragraph.\n");
        sb.append("Return ONLY valid JSON in the following format:\n");
        sb.append("{\n");
        sb.append("  \"grammarScore\": 82,\n");
        sb.append("  \"vocabularyScore\": 78,\n");
        sb.append("  \"fluencyScore\": 80,\n");
        sb.append("  \"clarityScore\": 85,\n");
        sb.append("  \"relevanceScore\": 88,\n");
        sb.append("  \"confidenceScore\": 76,\n");
        sb.append("  \"overallScore\": 81,\n");
        sb.append("  \"strengths\": [\"...\"],\n");
        sb.append("  \"improvements\": [\"...\"],\n");
        sb.append("  \"feedback\": \"...\"\n");
        sb.append("}");

        return sb.toString();
    }
}
