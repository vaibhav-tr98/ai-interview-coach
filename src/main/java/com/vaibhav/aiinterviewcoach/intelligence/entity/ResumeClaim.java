package com.vaibhav.aiinterviewcoach.intelligence.entity;

import com.vaibhav.aiinterviewcoach.intelligence.enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resume_claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String claimText;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String verificationQuestions;
}

