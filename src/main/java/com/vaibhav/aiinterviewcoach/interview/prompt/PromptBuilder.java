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

        prompt.append("\nInterview Type: ")
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
}