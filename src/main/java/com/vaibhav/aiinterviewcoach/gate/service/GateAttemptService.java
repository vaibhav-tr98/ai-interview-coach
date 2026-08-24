package com.vaibhav.aiinterviewcoach.gate.service;

import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.gate.dto.GateAttemptRequest;
import com.vaibhav.aiinterviewcoach.gate.dto.GateAttemptResponse;
import com.vaibhav.aiinterviewcoach.gate.entity.GateAttempt;
import com.vaibhav.aiinterviewcoach.gate.entity.GateQuestion;
import com.vaibhav.aiinterviewcoach.gate.repository.GateAttemptRepository;
import com.vaibhav.aiinterviewcoach.gate.repository.GateQuestionRepository;
import com.vaibhav.aiinterviewcoach.progress.entity.UserSkillProgress;
import com.vaibhav.aiinterviewcoach.progress.repository.UserSkillProgressRepository;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class GateAttemptService {

    private final GateAttemptRepository attemptRepository;
    private final GateQuestionRepository questionRepository;
    private final UserSkillProgressRepository userSkillProgressRepository;
    private final UserRepository userRepository;

    public GateAttemptService(GateAttemptRepository attemptRepository, GateQuestionRepository questionRepository,
                              UserSkillProgressRepository userSkillProgressRepository, UserRepository userRepository) {
        this.attemptRepository = attemptRepository;
        this.questionRepository = questionRepository;
        this.userSkillProgressRepository = userSkillProgressRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public GateAttemptResponse submitAttempt(Long questionId, GateAttemptRequest request) {
        User user = getCurrentUser();
        GateQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

        boolean isCorrect = question.getCorrectOption().equalsIgnoreCase(request.getSelectedAnswer());
        int score = isCorrect ? 100 : 0; // Simple binary scoring for MVP

        GateAttempt attempt = GateAttempt.builder()
                .user(user)
                .question(question)
                .selectedAnswer(request.getSelectedAnswer())
                .isCorrect(isCorrect)
                .score(score)
                .build();

        attempt = attemptRepository.save(attempt);

        // Update M7 UserSkillProgress
        updateUserSkillProgress(user, question.getSkill(), score);

        return GateAttemptResponse.builder()
                .attemptId(attempt.getId())
                .questionId(question.getId())
                .selectedAnswer(attempt.getSelectedAnswer())
                .correctOption(question.getCorrectOption())
                .isCorrect(attempt.getIsCorrect())
                .score(attempt.getScore())
                .build();
    }

    private void updateUserSkillProgress(User user, com.vaibhav.aiinterviewcoach.progress.entity.Skill skill, int newScore) {
        Optional<UserSkillProgress> optProgress = userSkillProgressRepository.findByUserIdAndSkillId(user.getId(), skill.getId());
        UserSkillProgress progress;

        if (optProgress.isPresent()) {
            progress = optProgress.get();
            double totalScore = (progress.getAverageScore() * progress.getAttemptCount()) + newScore;
            progress.setAttemptCount(progress.getAttemptCount() + 1);
            progress.setAverageScore(totalScore / progress.getAttemptCount());
            
            if (newScore > progress.getBestScore()) {
                progress.setBestScore(newScore);
            }
            if (newScore < progress.getWeakestScore()) {
                progress.setWeakestScore(newScore);
            }
            progress.setLastPracticedAt(LocalDateTime.now());
        } else {
            progress = UserSkillProgress.builder()
                    .user(user)
                    .skill(skill)
                    .averageScore((double) newScore)
                    .attemptCount(1)
                    .bestScore(newScore)
                    .weakestScore(newScore)
                    .lastPracticedAt(LocalDateTime.now())
                    .build();
        }
        userSkillProgressRepository.save(progress);
    }
}
