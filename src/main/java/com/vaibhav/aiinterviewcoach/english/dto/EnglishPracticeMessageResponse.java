package com.vaibhav.aiinterviewcoach.english.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnglishPracticeMessageResponse {
    private Long id;
    private String participantRole;
    private String messageText;
    private Integer sequenceNumber;
    private LocalDateTime createdAt;
}
