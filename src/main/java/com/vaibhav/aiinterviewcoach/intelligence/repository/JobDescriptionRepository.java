package com.vaibhav.aiinterviewcoach.intelligence.repository;

import com.vaibhav.aiinterviewcoach.intelligence.entity.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
    List<JobDescription> findByUserId(Long userId);
}

