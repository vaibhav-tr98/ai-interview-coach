package com.vaibhav.aiinterviewcoach.intelligence.entity;

import com.vaibhav.aiinterviewcoach.progress.entity.Skill;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resume_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    private Integer confidence; // 0-100
    
    @Column(columnDefinition = "TEXT")
    private String evidence;
}

