package com.vaibhav.aiinterviewcoach.english.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnglishPracticeSessionRequest {
    @NotBlank(message = "participantOneRole is required")
    private String participantOneRole;

    @NotBlank(message = "participantTwoRole is required")
    private String participantTwoRole;

    @NotBlank(message = "topic is required")
    private String topic;
}
