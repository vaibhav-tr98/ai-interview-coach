package com.vaibhav.aiinterviewcoach.planner.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "study_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_plan_id", nullable = false)
    private StudyPlan studyPlan;

    @Column(nullable = false)
    private Integer dayNumber;

    @Column(nullable = false, length = 100)
    private String skillName;

    @Column(nullable = false, length = 200)
    private String topic;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;
}
