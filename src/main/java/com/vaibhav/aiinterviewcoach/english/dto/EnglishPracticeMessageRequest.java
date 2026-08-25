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
public class EnglishPracticeMessageRequest {
    @NotBlank(message = "participantRole is required")
    private String participantRole;

    @NotBlank(message = "messageText is required")
    private String messageText;
}
