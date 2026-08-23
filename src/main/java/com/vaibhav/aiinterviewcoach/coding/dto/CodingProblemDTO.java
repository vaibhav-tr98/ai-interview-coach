package com.vaibhav.aiinterviewcoach.coding.dto;

import com.vaibhav.aiinterviewcoach.coding.enums.Difficulty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CodingProblemDTO {
    private Long id;
    private String title;
    private String description;
    private Difficulty difficulty;
    private Long skillId;
    private String skillName;
    private String constraints;
    private String examples;
    private String expectedInputFormat;
    private String expectedOutputFormat;
    private LocalDateTime createdAt;
}
