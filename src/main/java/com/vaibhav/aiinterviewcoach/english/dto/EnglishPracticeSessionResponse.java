package com.vaibhav.aiinterviewcoach.english.dto;

import com.vaibhav.aiinterviewcoach.english.enums.EnglishPracticeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnglishPracticeSessionResponse {
    private Long id;
    private String participantOneRole;
    private String participantTwoRole;
    private String topic;
    private EnglishPracticeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
