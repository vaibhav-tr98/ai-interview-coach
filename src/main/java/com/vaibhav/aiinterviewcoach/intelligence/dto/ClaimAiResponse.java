package com.vaibhav.aiinterviewcoach.intelligence.dto;

import lombok.Data;
import java.util.List;

@Data
public class ClaimAiResponse {
    private List<ClaimItem> claims;

    @Data
    public static class ClaimItem {
        private String claimText;
        private String status;
        private String verificationQuestions;
    }
}

