package com.vaibhav.aiinterviewcoach.progress.entity;

import com.vaibhav.aiinterviewcoach.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_skill_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "skill_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkillProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private Double averageScore;

    @Column(nullable = false)
    private Integer attemptCount;

    @Column(nullable = false)
    private Integer bestScore;

    @Column(nullable = false)
    private Integer weakestScore;

    @Column(nullable = false)
    private LocalDateTime lastPracticedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
