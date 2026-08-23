package com.vaibhav.aiinterviewcoach.coding.entity;

import com.vaibhav.aiinterviewcoach.coding.enums.SubmissionStatus;
import com.vaibhav.aiinterviewcoach.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "coding_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private CodingProblem problem;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Column(nullable = false, length = 50)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    private Integer score;

    private Long executionTimeMs;

    private Long memoryUsedKb;

    @Column(columnDefinition = "TEXT")
    private String testCaseResults;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
