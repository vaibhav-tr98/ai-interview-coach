package com.vaibhav.aiinterviewcoach.intelligence.prompt;

import org.springframework.stereotype.Component;

@Component
public class IntelligencePromptBuilder {

    public String buildResumeAnalysisPrompt(String resumeText, String allowedSkillsStr) {
        return """
            Analyze the following resume and extract the key information.
            
            Resume Text:
            %s
            
            Allowed Canonical Skills:
            %s
            
            Return the output in STRICT JSON format exactly matching this structure:
            {
              "summary": "A brief summary of the candidate (max 2 sentences).",
              "experienceLevel": "FRESHER, JUNIOR, MID_LEVEL, or SENIOR",
              "skills": [
                {
                  "skill": "SKILL_NAME",
                  "confidence": 95
                }
              ]
            }
            
            Rules:
            1. DO NOT use markdown formatting (no ```json).
            2. For skills, ONLY select from the Allowed Canonical Skills list. 
            3. If the candidate has a skill not in the allowed list, IGNORE IT.
            4. The confidence score should be 0-100 based on how strongly the resume demonstrates the skill.
            5. ONLY return valid JSON.
            """.formatted(resumeText, allowedSkillsStr);
    }

    public String buildJobDescriptionAnalysisPrompt(String jdText, String allowedSkillsStr) {
        return """
            Analyze the following job description and extract the key information.
            
            Job Description Text:
            %s
            
            Allowed Canonical Skills:
            %s
            
            Return the output in STRICT JSON format exactly matching this structure:
            {
              "company": "Company Name",
              "role": "Role Title",
              "seniority": "FRESHER, JUNIOR, MID_LEVEL, or SENIOR",
              "summary": "Brief summary of the role (max 2 sentences)",
              "requiredSkills": [
                {
                  "skill": "SKILL_NAME",
                  "importance": 90
                }
              ],
              "preferredSkills": [
                {
                  "skill": "SKILL_NAME",
                  "importance": 50
                }
              ]
            }
            
            Rules:
            1. DO NOT use markdown formatting (no ```json).
            2. For requiredSkills and preferredSkills, ONLY select from the Allowed Canonical Skills list.
            3. If the job requires a skill not in the allowed list, IGNORE IT.
            4. The importance score should be 0-100 based on how critical the skill is for the role.
            5. ONLY return valid JSON.
            """.formatted(jdText, allowedSkillsStr);
    }
    
    public String buildClaimVerificationPrompt(String resumeText) {
        return """
            Analyze the following resume and identify strong claims that require verification during an interview.
            Do not assume the candidate is lying, just highlight important technical claims that need validation.
            
            Resume Text:
            %s
            
            Return the output in STRICT JSON format exactly matching this structure:
            {
              "claims": [
                {
                  "claimText": "Built a microservices architecture handling 10k RPS",
                  "status": "NEEDS_VERIFICATION",
                  "verificationQuestions": "How did you measure the 10k RPS? What was the bottleneck? How did you handle distributed transactions?"
                }
              ]
            }
            
            Rules:
            1. DO NOT use markdown formatting (no ```json).
            2. Status must be one of: NEEDS_VERIFICATION, SUPPORTED_BY_CONTEXT, INSUFFICIENT_CONTEXT.
            3. Generate 1-3 insightful verification questions for NEEDS_VERIFICATION or INSUFFICIENT_CONTEXT claims.
            4. Extract a maximum of 5 critical claims.
            5. ONLY return valid JSON.
            """.formatted(resumeText);
    }
}

