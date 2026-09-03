package com.vaibhav.aiinterviewcoach.interview.prompt;

import com.vaibhav.aiinterviewcoach.interview.enums.Difficulty;
import com.vaibhav.aiinterviewcoach.interview.enums.DsaTopic;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewerPersona;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewType;
import lombok.Builder;

import java.util.List;

@Builder
public record InterviewContext(
        InterviewType interviewType,
        String role,
        String experienceLevel,
        Difficulty difficulty,
        DsaTopic dsaTopic,
        String resumeText,
        String jobDescription,
        String projectDescription,
        String projectUrl,
        Integer durationMinutes,
        InterviewerPersona interviewerPersona,
        List<String> weakSkills
) {}
