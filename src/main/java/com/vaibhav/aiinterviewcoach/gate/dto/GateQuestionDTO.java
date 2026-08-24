package com.vaibhav.aiinterviewcoach.gate.dto;

import com.vaibhav.aiinterviewcoach.gate.enums.GateDifficulty;
import com.vaibhav.aiinterviewcoach.gate.enums.GateQuestionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GateQuestionDTO {
    private Long id;
    private String subject;
    private String topic;
    private String questionText;
    private GateQuestionType questionType;
    private GateDifficulty difficulty;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;
    private String explanation;
}
