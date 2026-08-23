package com.vaibhav.aiinterviewcoach.coding.dto;

import com.vaibhav.aiinterviewcoach.coding.enums.SubmissionStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CodeExecutionResult {
    private SubmissionStatus status;
    private Integer score;
    private Long executionTimeMs;
    private Long memoryUsedKb;
    private String testCaseResults;
}
