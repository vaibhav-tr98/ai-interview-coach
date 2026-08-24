package com.vaibhav.aiinterviewcoach.gate.repository;

import com.vaibhav.aiinterviewcoach.gate.entity.GateAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GateAttemptRepository extends JpaRepository<GateAttempt, Long> {
    List<GateAttempt> findByUserId(Long userId);
}
