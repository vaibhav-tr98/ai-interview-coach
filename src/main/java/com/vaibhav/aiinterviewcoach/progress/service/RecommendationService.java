package com.vaibhav.aiinterviewcoach.progress.service;

import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.progress.dto.RecommendationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final WeaknessAnalysisService weaknessAnalysisService;

    private static final Map<String, Set<String>> ROLE_SKILLS_MAP = Map.of(
            "JAVA BACKEND DEVELOPER", Set.of("JAVA", "SPRING_BOOT", "SPRING_SECURITY", "REST_API", "JPA", "HIBERNATE", "SQL", "POSTGRESQL", "DATABASE_DESIGN", "SYSTEM_DESIGN"),
            "SPRING BOOT DEVELOPER", Set.of("JAVA", "SPRING_BOOT", "SPRING_SECURITY", "JPA", "HIBERNATE", "REST_API", "TRANSACTIONS", "SQL"),
            "MERN DEVELOPER", Set.of("JAVASCRIPT", "TYPESCRIPT", "REACT", "NODE_JS", "EXPRESS", "MONGODB"),
            "DSA PREPARATION", Set.of("ARRAYS", "STRINGS", "HASHING", "LINKED_LIST", "STACKS", "QUEUES", "TREES", "GRAPHS", "DYNAMIC_PROGRAMMING", "GREEDY", "BINARY_SEARCH")
    );

    public List<RecommendationDTO> getRecommendations(User user, String targetRole) {
        List<WeaknessAnalysisService.Weakness> weaknesses = weaknessAnalysisService.analyzeWeaknesses(user);

        Set<String> roleSkills = null;
        if (targetRole != null && !targetRole.isBlank()) {
            roleSkills = ROLE_SKILLS_MAP.get(targetRole.toUpperCase());
        }

        final Set<String> targetRoleSkills = roleSkills;

        return weaknesses.stream()
                .map(w -> {
                    double finalScore = w.weaknessScore();
                    String reason = w.reason();
                    String priority = w.priority();

                    if (targetRoleSkills != null && targetRoleSkills.contains(w.skillName())) {
                        finalScore += 50.0; // Boost priority for role-relevant skills
                        priority = "HIGH";
                        reason = "Repeated low performance in a skill relevant to the target role.";
                    }

                    return new PrioritizedRecommendation(w.skillName(), priority, reason, finalScore);
                })
                .sorted((r1, r2) -> Double.compare(r2.score, r1.score))
                .limit(5)
                .map(r -> new RecommendationDTO(r.skillName, r.priority, r.reason))
                .collect(Collectors.toList());
    }

    private record PrioritizedRecommendation(String skillName, String priority, String reason, double score) {}
}
