package com.vaibhav.aiinterviewcoach.english.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnglishPracticeSessionDetailResponse {
    private EnglishPracticeSessionResponse session;
    private List<EnglishPracticeMessageResponse> messages;
    private EnglishPracticeEvaluationResponse evaluation;
}
