package com.vaibhav.aiinterviewcoach.repository;

import com.vaibhav.aiinterviewcoach.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}