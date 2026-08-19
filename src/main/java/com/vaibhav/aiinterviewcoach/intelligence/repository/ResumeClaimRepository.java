package com.vaibhav.aiinterviewcoach.intelligence.repository;

import com.vaibhav.aiinterviewcoach.intelligence.entity.ResumeClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeClaimRepository extends JpaRepository<ResumeClaim, Long> {
    List<ResumeClaim> findByResumeId(Long resumeId);
    void deleteByResumeId(Long resumeId);
}

