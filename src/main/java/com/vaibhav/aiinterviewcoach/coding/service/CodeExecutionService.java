package com.vaibhav.aiinterviewcoach.coding.service;

import com.vaibhav.aiinterviewcoach.coding.dto.CodeExecutionResult;
import com.vaibhav.aiinterviewcoach.coding.entity.CodingProblem;

public interface CodeExecutionService {
    /**
     * Executes the provided Java code securely.
     * In MVP, this is mocked to prevent unauthorized execution.
     * 
     * @param code The Java source code.
     * @param problem The problem definition for evaluating test cases.
     * @return Result of the execution.
     */
    CodeExecutionResult execute(String code, CodingProblem problem);
}
