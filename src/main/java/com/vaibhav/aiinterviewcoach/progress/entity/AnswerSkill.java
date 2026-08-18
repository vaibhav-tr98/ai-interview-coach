package com.vaibhav.aiinterviewcoach.progress.entity;

import com.vaibhav.aiinterviewcoach.interview.entity.QuestionAnswer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "answer_skills", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"question_answer_id", "skill_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_answer_id", nullable = false)
    private QuestionAnswer questionAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private Integer relevanceScore;

    @Column(nullable = false)
    private Integer skillScore;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
