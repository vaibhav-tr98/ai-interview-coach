package com.vaibhav.aiinterviewcoach.planner.prompt;

import org.springframework.stereotype.Component;

@Component
public class PlannerPromptBuilder {

    public String buildStudyPlanPrompt(String targetRole, String weaknessesStr, String allowedSkillsStr) {
        return """
            You are an expert technical interviewer and career coach.
            Generate a personalized 7-day study plan to help the user overcome their weaknesses and prepare for their target role.
            
            Target Role:
            %s
            
            User's Top Weaknesses:
            %s
            
            Allowed Canonical Skills:
            %s
            
            Return the output in STRICT JSON format exactly matching this structure:
            {
              "targetRole": "Role Title",
              "days": [
                {
                  "dayNumber": 1,
                  "skill": "SKILL_NAME",
                  "topic": "Topic Title",
                  "description": "Actionable task description..."
                }
              ]
            }
            
            Rules:
            1. DO NOT use markdown formatting (no ```json).
            2. For the "skill" field, ONLY select from the Allowed Canonical Skills list.
            3. DO NOT invent or fabricate any skill names that are not in the allowed list.
            4. Focus heavily on the User's Top Weaknesses provided.
            5. Create exactly 7 days of tasks.
            6. Keep task descriptions highly actionable and interview-oriented.
            7. ONLY return valid JSON.
            """.formatted(targetRole, weaknessesStr, allowedSkillsStr);
    }
}
