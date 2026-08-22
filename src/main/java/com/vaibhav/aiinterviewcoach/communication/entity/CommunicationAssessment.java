package com.vaibhav.aiinterviewcoach.communication.entity;

import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.interview.entity.Interview;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "communication_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunicationAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false, unique = true)
    private Interview interview;

    @Min(0) @Max(100)
    private Integer overallScore;

    @Min(0) @Max(100)
    private Integer clarityScore;

    @Min(0) @Max(100)
    private Integer relevanceScore;

    @Min(0) @Max(100)
    private Integer completenessScore;

    @Min(0) @Max(100)
    private Integer concisenessScore;

    @Min(0) @Max(100)
    private Integer vocabularyScore;

    @Min(0) @Max(100)
    private Integer confidenceScore;

    private Integer fillerWordCount;

    private Integer repetitionCount;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weaknesses;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    private LocalDateTime assessedAt;

    @PrePersist
    public void onCreate() {
        assessedAt = LocalDateTime.now();
    }
}
