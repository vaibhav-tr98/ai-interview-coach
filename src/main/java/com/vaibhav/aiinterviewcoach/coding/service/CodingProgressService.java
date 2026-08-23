package com.vaibhav.aiinterviewcoach.coding.service;

import com.vaibhav.aiinterviewcoach.coding.dto.CodingProgressDTO;
import com.vaibhav.aiinterviewcoach.coding.entity.CodingSubmission;
import com.vaibhav.aiinterviewcoach.coding.enums.SubmissionStatus;
import com.vaibhav.aiinterviewcoach.coding.repository.CodingSubmissionRepository;
import com.vaibhav.aiinterviewcoach.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodingProgressService {

    private final CodingSubmissionRepository submissionRepository;

    public CodingProgressDTO getProgress(User user) {
        List<CodingSubmission> submissions = submissionRepository.findByUserId(user.getId());

        if (submissions.isEmpty()) {
            return CodingProgressDTO.builder()
                    .attemptedProblems(0)
                    .solvedProblems(0)
                    .successRate(0.0)
                    .averageScore(0.0)
                    .topicPerformance(new HashMap<>())
                    .difficultyDistribution(new HashMap<>())
                    .build();
        }

        int totalAttempts = submissions.size();
        
        long solvedProblems = submissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                .map(s -> s.getProblem().getId())
                .distinct()
                .count();

        long attemptedProblems = submissions.stream()
                .map(s -> s.getProblem().getId())
                .distinct()
                .count();

        double averageScore = submissions.stream()
                .filter(s -> s.getScore() != null)
                .mapToInt(CodingSubmission::getScore)
                .average()
                .orElse(0.0);

        double successRate = ((double) solvedProblems / (attemptedProblems == 0 ? 1 : attemptedProblems)) * 100;

        Map<String, Integer> difficultyDistribution = new HashMap<>();
        Map<String, CodingProgressDTO.TopicStats> topicPerformance = new HashMap<>();

        Map<String, List<CodingSubmission>> byTopic = submissions.stream()
                .collect(Collectors.groupingBy(s -> s.getProblem().getSkill().getName()));

        for (Map.Entry<String, List<CodingSubmission>> entry : byTopic.entrySet()) {
            String topic = entry.getKey();
            List<CodingSubmission> topicSubs = entry.getValue();

            double topicAvg = topicSubs.stream()
                    .filter(s -> s.getScore() != null)
                    .mapToInt(CodingSubmission::getScore)
                    .average()
                    .orElse(0.0);

            topicPerformance.put(topic, CodingProgressDTO.TopicStats.builder()
                    .attempts(topicSubs.size())
                    .averageScore(topicAvg)
                    .build());
        }

        submissions.forEach(s -> {
            String diff = s.getProblem().getDifficulty().name();
            difficultyDistribution.put(diff, difficultyDistribution.getOrDefault(diff, 0) + 1);
        });

        return CodingProgressDTO.builder()
                .attemptedProblems((int) attemptedProblems)
                .solvedProblems((int) solvedProblems)
                .successRate(successRate)
                .averageScore(averageScore)
                .topicPerformance(topicPerformance)
                .difficultyDistribution(difficultyDistribution)
                .build();
    }
}
