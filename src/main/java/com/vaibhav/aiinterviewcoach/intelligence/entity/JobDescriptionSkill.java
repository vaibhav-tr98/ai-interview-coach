package com.vaibhav.aiinterviewcoach.intelligence.entity;

import com.vaibhav.aiinterviewcoach.progress.entity.Skill;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_description_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDescriptionSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_description_id", nullable = false)
    private JobDescription jobDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    private Integer importance; // 0-100
    
    private Boolean isRequired;
}

