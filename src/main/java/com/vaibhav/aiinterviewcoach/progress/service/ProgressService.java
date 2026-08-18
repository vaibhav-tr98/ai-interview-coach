package com.vaibhav.aiinterviewcoach.progress.service;

import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.interview.dto.SkillEvaluationDTO;
import com.vaibhav.aiinterviewcoach.interview.entity.QuestionAnswer;
import com.vaibhav.aiinterviewcoach.progress.entity.AnswerSkill;
import com.vaibhav.aiinterviewcoach.progress.entity.Skill;
import com.vaibhav.aiinterviewcoach.progress.entity.UserSkillProgress;
import com.vaibhav.aiinterviewcoach.progress.repository.AnswerSkillRepository;
import com.vaibhav.aiinterviewcoach.progress.repository.SkillRepository;
import com.vaibhav.aiinterviewcoach.progress.repository.UserSkillProgressRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private static final Logger log = LoggerFactory.getLogger(ProgressService.class);

    private final SkillRepository skillRepository;
    private final AnswerSkillRepository answerSkillRepository;
    private final UserSkillProgressRepository userSkillProgressRepository;
    private final RecommendationService recommendationService;
    private final com.vaibhav.aiinterviewcoach.interview.repository.InterviewRepository interviewRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAnswerSkills(User user, QuestionAnswer questionAnswer, List<SkillEvaluationDTO> evaluatedSkills) {
        if (evaluatedSkills == null || evaluatedSkills.isEmpty()) {
            return;
        }

        Set<String> processedSkills = new HashSet<>();

        for (SkillEvaluationDTO skillEval : evaluatedSkills) {
            String skillName = skillEval.skill();
            if (skillName == null || skillName.isBlank()) continue;

            // Prevent duplicate skill processing for the same answer
            if (!processedSkills.add(skillName)) continue;

            Optional<Skill> skillOpt = skillRepository.findByName(skillName);
            if (skillOpt.isEmpty()) {
                log.warn("Unknown skill returned by AI: {}. Ignoring.", skillName);
                continue;
            }

            Skill skill = skillOpt.get();

            try {
                // 1. Create AnswerSkill mapping
                AnswerSkill answerSkill = AnswerSkill.builder()
                        .questionAnswer(questionAnswer)
                        .skill(skill)
                        .relevanceScore(skillEval.relevance())
                        .skillScore(skillEval.score())
                        .build();
                answerSkillRepository.save(answerSkill);

                // 2. Update UserSkillProgress
                updateUserSkillProgress(user, skill);
            } catch (Exception e) {
                log.error("Failed to process skill {} for answer id {}: {}", skillName, questionAnswer.getId(), e.getMessage());
            }
        }
    }

    private void updateUserSkillProgress(User user, Skill skill) {
        List<AnswerSkill> historicalSkills = answerSkillRepository.findHistoryBySkillAndUserOrdered(skill.getId(), user.getId());

        if (historicalSkills.isEmpty()) return;

        double totalScore = 0;
        int maxScore = 0;
        int minScore = 100;
        int count = historicalSkills.size();
        LocalDateTime latest = historicalSkills.get(0).getCreatedAt();

        for (AnswerSkill as : historicalSkills) {
            int score = as.getSkillScore();
            totalScore += score;
            if (score > maxScore) maxScore = score;
            if (score < minScore) minScore = score;
            if (as.getCreatedAt() != null && (latest == null || as.getCreatedAt().isAfter(latest))) {
                latest = as.getCreatedAt();
            }
        }

        double avgScore = totalScore / count;

        UserSkillProgress progress = userSkillProgressRepository.findByUserIdAndSkillId(user.getId(), skill.getId())
                .orElse(UserSkillProgress.builder()
                        .user(user)
                        .skill(skill)
                        .build());

        progress.setAverageScore(avgScore);
        progress.setAttemptCount(count);
        progress.setBestScore(maxScore);
        progress.setWeakestScore(minScore);
        progress.setLastPracticedAt(latest);

        userSkillProgressRepository.save(progress);
    }
    
    public com.vaibhav.aiinterviewcoach.progress.dto.OverallProgressResponse getOverallProgress(User user) {
        List<UserSkillProgress> progresses = userSkillProgressRepository.findByUserId(user.getId());

        List<com.vaibhav.aiinterviewcoach.interview.entity.Interview> userInterviews = interviewRepository.findByUser(user);

        int totalInterviews = userInterviews.size();
        long completedInterviews = userInterviews.stream()
                .filter(i -> i.getStatus() == com.vaibhav.aiinterviewcoach.interview.enums.InterviewStatus.COMPLETED)
                .count();

        List<com.vaibhav.aiinterviewcoach.progress.dto.SkillScoreDTO> strongest = progresses.stream()
                .sorted((a, b) -> Double.compare(b.getAverageScore(), a.getAverageScore()))
                .limit(3)
                .map(p -> new com.vaibhav.aiinterviewcoach.progress.dto.SkillScoreDTO(p.getSkill().getName(), (int) Math.round(p.getAverageScore())))
                .toList();

        List<com.vaibhav.aiinterviewcoach.progress.dto.SkillScoreDTO> weakest = progresses.stream()
                .sorted((a, b) -> Double.compare(a.getAverageScore(), b.getAverageScore()))
                .limit(3)
                .map(p -> new com.vaibhav.aiinterviewcoach.progress.dto.SkillScoreDTO(p.getSkill().getName(), (int) Math.round(p.getAverageScore())))
                .toList();

        int overallScore = progresses.isEmpty() ? 0 :
                (int) Math.round(progresses.stream().mapToDouble(UserSkillProgress::getAverageScore).average().orElse(0));

        // Recommendations
        String latestRole = userInterviews.stream()
                .filter(i -> i.getRole() != null)
                .max((i1, i2) -> i1.getCreatedAt().compareTo(i2.getCreatedAt()))
                .map(com.vaibhav.aiinterviewcoach.interview.entity.Interview::getRole)
                .orElse(null);

        List<com.vaibhav.aiinterviewcoach.progress.dto.RecommendationDTO> recommendations = recommendationService.getRecommendations(user, latestRole);

        List<String> improvingSkills = progresses.stream()
                .filter(p -> "IMPROVING".equals(calculateTrend(p)))
                .map(p -> p.getSkill().getName())
                .toList();

        List<String> decliningSkills = progresses.stream()
                .filter(p -> "DECLINING".equals(calculateTrend(p)))
                .map(p -> p.getSkill().getName())
                .toList();

        return new com.vaibhav.aiinterviewcoach.progress.dto.OverallProgressResponse(
                overallScore,
                totalInterviews,
                (int) completedInterviews,
                overallScore, // Simplified average interview score
                overallScore, // Simplified best
                overallScore, // Simplified latest
                strongest,
                weakest,
                improvingSkills,
                decliningSkills,
                recommendations
        );
    }

    public List<com.vaibhav.aiinterviewcoach.progress.dto.SkillProgressResponse> getPracticedSkills(User user) {
        return userSkillProgressRepository.findByUserId(user.getId()).stream()
                .map(p -> new com.vaibhav.aiinterviewcoach.progress.dto.SkillProgressResponse(
                        p.getSkill().getName(),
                        p.getAverageScore(),
                        p.getBestScore(),
                        p.getWeakestScore(),
                        p.getAttemptCount(),
                        p.getLastPracticedAt(),
                        calculateTrend(p)
                ))
                .toList();
    }

    public com.vaibhav.aiinterviewcoach.progress.dto.SkillProgressResponse getSkillProgress(User user, String skillName) {
        UserSkillProgress p = userSkillProgressRepository.findByUserIdAndSkillName(user.getId(), skillName.toUpperCase())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Skill progress not found"));

        return new com.vaibhav.aiinterviewcoach.progress.dto.SkillProgressResponse(
                p.getSkill().getName(),
                p.getAverageScore(),
                p.getBestScore(),
                p.getWeakestScore(),
                p.getAttemptCount(),
                p.getLastPracticedAt(),
                calculateTrend(p)
        );
    }

    public String calculateTrend(UserSkillProgress progress) {
        if (progress.getAttemptCount() < 2) {
            return "INSUFFICIENT_DATA";
        }
        
        List<AnswerSkill> history = answerSkillRepository.findHistoryBySkillAndUserOrdered(progress.getSkill().getId(), progress.getUser().getId());
                
        if (history.size() < 2) return "INSUFFICIENT_DATA";

        int mid = history.size() / 2;
        double firstHalfAvg = history.subList(0, mid).stream().mapToInt(AnswerSkill::getSkillScore).average().orElse(0);
        double secondHalfAvg = history.subList(mid, history.size()).stream().mapToInt(AnswerSkill::getSkillScore).average().orElse(0);

        if (secondHalfAvg > firstHalfAvg + 5) {
            return "IMPROVING";
        } else if (secondHalfAvg < firstHalfAvg - 5) {
            return "DECLINING";
        } else {
            return "STABLE";
        }
    }
}
