package com.vaibhav.aiinterviewcoach.gate.entity;

import com.vaibhav.aiinterviewcoach.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "gate_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private GateQuestion question;

    @Column(nullable = false)
    private String selectedAnswer;

    @Column(nullable = false)
    private Boolean isCorrect;

    @Column(nullable = false)
    private Integer score;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime attemptedAt;
}
