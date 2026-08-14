package com.vaibhav.aiinterviewcoach.interview.prompt;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildPrompt(
            String interviewType,
            String experience,
            String resume,
            String jobDescription,
            String projectDescription
    ) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                You are an expert technical interviewer.

                Rules:
                - Ask ONE question at a time.
                - Wait for the candidate's answer.
                - Do not reveal answers.
                - Be professional.
                """);

        prompt.append("\ninterview Type: ")
                .append(interviewType);

        prompt.append("\nExperience: ")
                .append(experience);

        if (resume != null && !resume.isBlank()) {
            prompt.append("\nResume:\n")
                    .append(resume);
        }

        if (jobDescription != null && !jobDescription.isBlank()) {
            prompt.append("\nJob Description:\n")
                    .append(jobDescription);
        }

        if (projectDescription != null &&
                !projectDescription.isBlank()) {

            prompt.append("\nProject:\n")
                    .append(projectDescription);
        }

        prompt.append("""
                
                Ask the first interview question.
                """);

        return prompt.toString();
    }

    public String buildNextQuestionPrompt(
            String interviewType,
            Integer nextQuestionNumber,
            String previousQuestion,
            String candidateAnswer
    ) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                You are an expert technical interviewer.

                Rules:
                - Ask ONE question at a time.
                - Wait for the candidate's answer.
                - Do not reveal answers.
                - Be professional.
                - Ask the next question based on the candidate's previous answer.
                """);

        prompt.append("\nInterview Type: ")
                .append(interviewType);

        prompt.append("\nPrevious Question: ")
                .append(previousQuestion);

        prompt.append("\nCandidate Answer: ")
                .append(candidateAnswer);

        prompt.append("\n\nAsk question number ")
                .append(nextQuestionNumber)
                .append(".\n");

        return prompt.toString();
    }

    public String buildAnswerEvaluationPrompt(
            String question,
            String answer,
            String interviewType
    ) {
        return """
                You are an expert technical interviewer. Evaluate the following candidate answer objectively.

                Interview Type: %s
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
                """.formatted(interviewType, question, answer);
    }
}