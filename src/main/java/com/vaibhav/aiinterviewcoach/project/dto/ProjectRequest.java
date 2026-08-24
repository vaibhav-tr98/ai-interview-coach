package com.vaibhav.aiinterviewcoach.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    private String techStack;
    private String projectUrl;
}
