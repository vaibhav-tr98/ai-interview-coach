package com.vaibhav.aiinterviewcoach.progress.service;

import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.progress.entity.UserSkillProgress;
import com.vaibhav.aiinterviewcoach.progress.repository.UserSkillProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeaknessAnalysisService {

    private final UserSkillProgressRepository userSkillProgressRepository;

    private final com.vaibhav.aiinterviewcoach.progress.repository.AnswerSkillRepository answerSkillRepository;

    public record Weakness(String skillName, double weaknessScore, String priority, String reason) {}

    public List<Weakness> analyzeWeaknesses(User user) {
        List<UserSkillProgress> progresses = userSkillProgressRepository.findByUserId(user.getId());

        return progresses.stream()
                .filter(p -> p.getAverageScore() < 70) // Threshold for weakness
                .map(this::calculateWeakness)
                .sorted((w1, w2) -> Double.compare(w2.weaknessScore(), w1.weaknessScore()))
                .collect(Collectors.toList());
    }

    private Weakness calculateWeakness(UserSkillProgress progress) {
        double avgScore = progress.getAverageScore();
        
        List<com.vaibhav.aiinterviewcoach.progress.entity.AnswerSkill> history = answerSkillRepository.findHistoryBySkillAndUserOrdered(
                progress.getSkill().getId(), progress.getUser().getId());

        double baseWeakness = 100 - avgScore;

        long weakAttemptsCount = history.stream().filter(h -> h.getSkillScore() < 70).count();
        double repeatedWeaknessBonus = weakAttemptsCount * 2.0;

        double recentWeaknessBonus = 0;
        if (!history.isEmpty() && history.get(history.size() - 1).getSkillScore() < 70) {
            recentWeaknessBonus = 10.0;
        }

        double trendBonus = 0;
        if (history.size() >= 2) {
            int mid = history.size() / 2;
            double firstHalfAvg = history.subList(0, mid).stream().mapToInt(com.vaibhav.aiinterviewcoach.progress.entity.AnswerSkill::getSkillScore).average().orElse(0);
            double secondHalfAvg = history.subList(mid, history.size()).stream().mapToInt(com.vaibhav.aiinterviewcoach.progress.entity.AnswerSkill::getSkillScore).average().orElse(0);

            if (secondHalfAvg < firstHalfAvg - 5) {
                trendBonus = 10.0; // DECLINING trend penalty
            }
        }

        double weaknessScore = baseWeakness + repeatedWeaknessBonus + recentWeaknessBonus + trendBonus;

        String priority;
        if (weaknessScore > 60) {
            priority = "HIGH";
        } else if (weaknessScore > 40) {
            priority = "MEDIUM";
        } else {
            priority = "LOW";
        }

        String reason = String.format("Average score of %.1f with %d weak attempts out of %d.", avgScore, weakAttemptsCount, history.size());
        if (recentWeaknessBonus > 0 && trendBonus > 0) {
            reason += " Recent performance is poor and trending downwards.";
        } else if (recentWeaknessBonus > 0) {
            reason += " Most recent attempt was poor.";
        } else if (trendBonus > 0) {
            reason += " Overall trend is declining.";
        }

        return new Weakness(progress.getSkill().getName(), weaknessScore, priority, reason);
    }
}
