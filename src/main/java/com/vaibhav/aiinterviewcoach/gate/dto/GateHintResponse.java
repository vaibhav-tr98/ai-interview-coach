package com.vaibhav.aiinterviewcoach.gate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GateHintResponse {
    private Long questionId;
    private String hint;
}
