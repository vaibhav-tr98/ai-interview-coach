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

    @com.fasterxml.jackson.annotation.JsonProperty("title")
    public String getTitle() {
        return activityType;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("date")
    public String getDate() {
        if (timestamp == null) return null;
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return timestamp.format(formatter);
    }
}
