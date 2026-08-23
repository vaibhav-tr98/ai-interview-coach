package com.vaibhav.aiinterviewcoach.coding.repository;

import com.vaibhav.aiinterviewcoach.coding.entity.CodingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodingSubmissionRepository extends JpaRepository<CodingSubmission, Long> {
    List<CodingSubmission> findByUserId(Long userId);
    List<CodingSubmission> findByUserIdAndProblemId(Long userId, Long problemId);
    Optional<CodingSubmission> findByIdAndUserId(Long id, Long userId);
}
