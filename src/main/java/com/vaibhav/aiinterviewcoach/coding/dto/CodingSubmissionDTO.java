package com.vaibhav.aiinterviewcoach.coding.dto;

import com.vaibhav.aiinterviewcoach.coding.enums.SubmissionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CodingSubmissionDTO {
    private Long id;
    private Long problemId;
    private String problemTitle;
    private String code;
    private String language;
    private SubmissionStatus status;
    private Integer score;
    private Long executionTimeMs;
    private Long memoryUsedKb;
    private String testCaseResults;
    private LocalDateTime createdAt;
}
