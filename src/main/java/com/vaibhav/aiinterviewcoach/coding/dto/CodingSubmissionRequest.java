package com.vaibhav.aiinterviewcoach.coding.dto;

import lombok.Data;

@Data
public class CodingSubmissionRequest {
    private String code;
    private String language = "JAVA";
}
