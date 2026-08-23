package com.vaibhav.aiinterviewcoach.coding.service;

import com.vaibhav.aiinterviewcoach.coding.dto.CodeExecutionResult;
import com.vaibhav.aiinterviewcoach.coding.entity.CodingProblem;
import com.vaibhav.aiinterviewcoach.coding.enums.SubmissionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockCodeExecutionService implements CodeExecutionService {

    private static final Logger log = LoggerFactory.getLogger(MockCodeExecutionService.class);

    @Override
    public CodeExecutionResult execute(String code, CodingProblem problem) {
        log.warn("MockCodeExecutionService invoked. This is a SIMULATED MVP execution result.");
        log.warn("Arbitrary user code is NEVER executed by the backend.");

        // We simulate a successful test pass with a score of 100 for MVP testing purposes.
        // We do NOT execute the submitted code.
        return CodeExecutionResult.builder()
                .status(SubmissionStatus.ACCEPTED)
                .score(100)
                .executionTimeMs(15L)
                .memoryUsedKb(2048L)
                .testCaseResults("[{\"input\": \"sample\", \"output\": \"sample\", \"passed\": true}]")
                .build();
    }
}
