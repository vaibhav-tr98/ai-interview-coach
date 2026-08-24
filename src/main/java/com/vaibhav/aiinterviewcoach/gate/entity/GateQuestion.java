package com.vaibhav.aiinterviewcoach.gate.entity;

import com.vaibhav.aiinterviewcoach.gate.enums.GateDifficulty;
import com.vaibhav.aiinterviewcoach.gate.enums.GateQuestionType;
import com.vaibhav.aiinterviewcoach.progress.entity.Skill;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "gate_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false, length = 2000)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GateQuestionType questionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GateDifficulty difficulty;

    @Column(length = 500)
    private String optionA;

    @Column(length = 500)
    private String optionB;

    @Column(length = 500)
    private String optionC;

    @Column(length = 500)
    private String optionD;

    @Column(nullable = false)
    private String correctOption; // "A", "B", "C", "D"

    @Column(length = 2000)
    private String explanation;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
