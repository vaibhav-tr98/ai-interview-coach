package com.vaibhav.aiinterviewcoach.gate.dto;

import com.vaibhav.aiinterviewcoach.gate.enums.GateDifficulty;
import com.vaibhav.aiinterviewcoach.gate.enums.GateQuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GateQuestionRequest {
    private String subject;
    private String topic;
    private GateDifficulty difficulty;
    private GateQuestionType questionType;
}
