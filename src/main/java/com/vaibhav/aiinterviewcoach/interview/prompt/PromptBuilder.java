package com.vaibhav.aiinterviewcoach.interview.prompt;

import com.vaibhav.aiinterviewcoach.interview.enums.InterviewerPersona;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {

    public String buildInitialQuestionPrompt(InterviewContext context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert technical interviewer.\n\n");
        
        appendRules(prompt);
        appendPersonaRules(prompt, context.interviewerPersona());
        appendContext(prompt, context);

        prompt.append("\nAsk the first interview question. Do NOT include any pleasantries or context in your response, just the question itself.");
        return prompt.toString();
    }

    public String buildNextQuestionPrompt(
            InterviewContext context,
            InterviewState state,
            List<InterviewTurnContext> history
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an expert technical interviewer.\n\n");
        
        appendRules(prompt);
        appendPersonaRules(prompt, context.interviewerPersona());
        appendContext(prompt, context);

        prompt.append("\n--- Interview History ---\n");
        if (history != null && !history.isEmpty()) {
            for (InterviewTurnContext turn : history) {
                prompt.append("Question ").append(turn.questionNumber()).append(": ").append(turn.question()).append("\n");
                prompt.append("Candidate Answer: ").append(turn.answer()).append("\n");
                prompt.append("Evaluation - Score: ").append(turn.evaluationScore()).append("\n");
                prompt.append("Evaluation - Feedback: ").append(turn.evaluationFeedback()).append("\n");
                prompt.append("Evaluation - Strengths: ").append(turn.evaluationStrengths()).append("\n");
                prompt.append("Evaluation - Weaknesses: ").append(turn.evaluationWeaknesses()).append("\n");
                prompt.append("-------------------------\n");
            }
        } else {
            prompt.append("No history available.\n");
        }

        prompt.append("\nYour task is to ask question number ").append(state.nextQuestionNumber());
        if (state.totalQuestions() != null) {
            prompt.append(" out of ").append(state.totalQuestions());
        }
        prompt.append(".\n\n");
        
        prompt.append("Based on the candidate's previous answer and evaluation, you should:\n");
        prompt.append("- Ask a deeper follow-up when the answer is strong.\n");
        prompt.append("- Ask clarification when the answer is vague.\n");
        prompt.append("- Probe weaknesses if they failed to answer completely.\n");
        prompt.append("- Change topic if appropriate, but remain within the configured interview scope.\n");
        prompt.append("- Avoid repeatedly asking the same question.\n");
        
        prompt.append("\nOutput ONLY the next question text. Do NOT reveal scores, evaluations, or coaching hints to the candidate.");

        return prompt.toString();
    }

    public String buildAnswerEvaluationPrompt(
            String question,
            String answer,
            InterviewContext context
    ) {
        return """
                You are an expert technical interviewer. Evaluate the following candidate answer objectively.

                Interview Type: %s
                Role: %s
                Experience Level: %s
                Question: %s
                Candidate Answer: %s

                Return ONLY valid JSON with this exact structure:
                {
                  "score": 0,
                  "feedback": "...",
                  "strengths": "...",
                  "weaknesses": "..."
                }

                Rules:
                - score must be an integer from 0 to 100.
                - feedback should briefly explain the quality/correctness of the answer.
                - strengths should identify what the candidate did well.
                - weaknesses should identify missing, incorrect, or weak areas.
                - Do not include markdown.
                - Do not include ```json.
                - Do not add fields outside the specified structure.
                - Do not evaluate based on grammar alone; prioritize technical correctness, completeness, clarity, and relevance.
                """.formatted(
                context.interviewType(),
                context.role() != null ? context.role() : "N/A",
                context.experienceLevel(),
                question,
                answer
        );
    }

    private void appendRules(StringBuilder prompt) {
        prompt.append("Rules:\n");
        prompt.append("- Ask ONE question at a time.\n");
        prompt.append("- Do not reveal answers.\n");
        prompt.append("- Do not provide scores or coaching hints during the live interview.\n");
        prompt.append("- Stay professional.\n");
        prompt.append("- Use candidate context when available.\n");
        prompt.append("- Do not invent candidate experience.\n");
        prompt.append("- If the candidate claims something in their context, ask clarifying/deep-dive questions.\n");
        prompt.append("- Keep questions relevant to the selected role/interview type.\n");
    }

    private void appendPersonaRules(StringBuilder prompt, InterviewerPersona persona) {
        if (persona == null) return;
        
        prompt.append("\nInterviewer Persona (").append(persona.name()).append("):\n");
        switch (persona) {
            case FRIENDLY:
                prompt.append("- Be supportive but professional.\n");
                prompt.append("- Use a conversational tone.\n");
                prompt.append("- Encourage explanation.\n");
                prompt.append("- Ask meaningful follow-ups without being intimidating.\n");
                break;
            case PROFESSIONAL:
                prompt.append("- Remain neutral and realistic.\n");
                prompt.append("- Be concise.\n");
                prompt.append("- Maintain balanced questioning.\n");
                break;
            case STRICT:
                prompt.append("- Be concise and challenging.\n");
                prompt.append("- Challenge vague answers.\n");
                prompt.append("- Ask precise follow-ups and probe weaknesses heavily.\n");
                prompt.append("- Do not give unnecessary encouragement.\n");
                break;
            case TECHNICAL:
                prompt.append("- Focus deeply on implementation details.\n");
                prompt.append("- Frequently ask \"why\" and trade-off questions.\n");
                prompt.append("- Probe architecture and core fundamentals.\n");
                break;
        }
    }

    private void appendContext(StringBuilder prompt, InterviewContext context) {
        prompt.append("\n--- Interview Configuration ---\n");
        prompt.append("Interview Type: ").append(context.interviewType()).append("\n");
        prompt.append("Experience Level: ").append(context.experienceLevel()).append("\n");
        
        if (context.role() != null && !context.role().isBlank()) {
            prompt.append("Target Role: ").append(context.role()).append("\n");
        }
        
        if (context.dsaTopic() != null) {
            prompt.append("DSA Topic: ").append(context.dsaTopic()).append("\n");
            if (context.difficulty() != null) {
                prompt.append("DSA Difficulty: ").append(context.difficulty()).append("\n");
            }
            prompt.append("- The question MUST respect the DSA Topic and Difficulty if specified.\n");
        }
        
        if (context.resumeText() != null && !context.resumeText().isBlank()) {
            prompt.append("\nCandidate Resume:\n").append(context.resumeText()).append("\n");
        }
        
        if (context.jobDescription() != null && !context.jobDescription().isBlank()) {
            prompt.append("\nJob Description:\n").append(context.jobDescription()).append("\n");
        }
        
        if (context.projectDescription() != null && !context.projectDescription().isBlank()) {
            prompt.append("\nCandidate Project Description:\n").append(context.projectDescription()).append("\n");
        }
        
        if (context.projectUrl() != null && !context.projectUrl().isBlank()) {
            prompt.append("Candidate Project URL: ").append(context.projectUrl()).append("\n");
        }
    }
}