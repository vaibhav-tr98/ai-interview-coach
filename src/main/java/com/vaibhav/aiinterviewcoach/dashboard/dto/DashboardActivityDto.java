package com.vaibhav.aiinterviewcoach.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardActivityDto {
    private String activityType;
    private String description;
    private LocalDateTime timestamp;
}
