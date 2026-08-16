package com.vaibhav.aiinterviewcoach.interview.entity;

import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.interview.enums.Difficulty;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewStatus;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(name = "dsa_topic")
    private com.vaibhav.aiinterviewcoach.interview.enums.DsaTopic dsaTopic;

    @Column(columnDefinition = "TEXT")
    private String resumeText;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    @Column(columnDefinition = "TEXT")
    private String projectDescription;

    private String projectUrl;

    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    private com.vaibhav.aiinterviewcoach.interview.enums.InterviewerPersona interviewerPersona;

    private String experienceLevel;

    private LocalDateTime completedAt;


    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = InterviewStatus.CREATED;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}