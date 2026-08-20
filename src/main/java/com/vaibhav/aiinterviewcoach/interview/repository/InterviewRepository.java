package com.vaibhav.aiinterviewcoach.interview.repository;

import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByUser(User user);
    List<Interview> findByUserIdOrderByCreatedAtDesc(Long userId);

}