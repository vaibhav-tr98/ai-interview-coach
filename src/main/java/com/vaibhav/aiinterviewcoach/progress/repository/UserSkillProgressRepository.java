package com.vaibhav.aiinterviewcoach.progress.repository;

import com.vaibhav.aiinterviewcoach.progress.entity.UserSkillProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillProgressRepository extends JpaRepository<UserSkillProgress, Long> {
    Optional<UserSkillProgress> findByUserIdAndSkillId(Long userId, Long skillId);
    Optional<UserSkillProgress> findByUserIdAndSkillName(Long userId, String skillName);
    List<UserSkillProgress> findByUserId(Long userId);
}
