package com.vaibhav.aiinterviewcoach.gate.service;

import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.gate.dto.GateProgressDTO;
import com.vaibhav.aiinterviewcoach.gate.dto.GateSubjectProgressDTO;
import com.vaibhav.aiinterviewcoach.gate.dto.GateTopicProgressDTO;
import com.vaibhav.aiinterviewcoach.gate.entity.GateAttempt;
import com.vaibhav.aiinterviewcoach.gate.repository.GateAttemptRepository;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GateProgressService {

    private final GateAttemptRepository attemptRepository;
    private final UserRepository userRepository;

    public GateProgressService(GateAttemptRepository attemptRepository, UserRepository userRepository) {
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public GateProgressDTO getProgress() {
        User user = getCurrentUser();
        List<GateAttempt> attempts = attemptRepository.findByUserId(user.getId());

        if (attempts.isEmpty()) {
            return GateProgressDTO.builder()
                    .totalAttempts(0)
                    .correctAttempts(0)
                    .incorrectAttempts(0)
                    .accuracy(0.0)
                    .averageScore(0.0)
                    .subjectWiseProgress(new ArrayList<>())
                    .build();
        }

        int totalAttempts = attempts.size();
        int correctAttempts = (int) attempts.stream().filter(GateAttempt::getIsCorrect).count();
        int incorrectAttempts = totalAttempts - correctAttempts;
        double accuracy = ((double) correctAttempts / totalAttempts) * 100.0;
        double averageScore = attempts.stream().mapToInt(GateAttempt::getScore).average().orElse(0.0);

        // Group by subject (skill name)
        Map<String, List<GateAttempt>> bySubject = attempts.stream()
                .collect(Collectors.groupingBy(a -> a.getQuestion().getSkill().getName()));

        List<GateSubjectProgressDTO> subjectProgressList = new ArrayList<>();
        String strongestSubject = null;
        String weakestSubject = null;
        double maxAccuracy = -1.0;
        double minAccuracy = 101.0;

        for (Map.Entry<String, List<GateAttempt>> entry : bySubject.entrySet()) {
            String subject = entry.getKey();
            List<GateAttempt> subjAttempts = entry.getValue();
            
            int sTotal = subjAttempts.size();
            int sCorrect = (int) subjAttempts.stream().filter(GateAttempt::getIsCorrect).count();
            double sAccuracy = ((double) sCorrect / sTotal) * 100.0;

            if (sAccuracy > maxAccuracy) {
                maxAccuracy = sAccuracy;
                strongestSubject = subject;
            }
            if (sAccuracy < minAccuracy) {
                minAccuracy = sAccuracy;
                weakestSubject = subject;
            }

            // Group by topic
            Map<String, List<GateAttempt>> byTopic = subjAttempts.stream()
                    .collect(Collectors.groupingBy(a -> a.getQuestion().getTopic()));
            
            List<GateTopicProgressDTO> topicProgressList = new ArrayList<>();
            for (Map.Entry<String, List<GateAttempt>> tEntry : byTopic.entrySet()) {
                int tTotal = tEntry.getValue().size();
                int tCorrect = (int) tEntry.getValue().stream().filter(GateAttempt::getIsCorrect).count();
                double tAccuracy = ((double) tCorrect / tTotal) * 100.0;
                
                topicProgressList.add(GateTopicProgressDTO.builder()
                        .topic(tEntry.getKey())
                        .totalAttempts(tTotal)
                        .correctAttempts(tCorrect)
                        .accuracy(tAccuracy)
                        .build());
            }

            subjectProgressList.add(GateSubjectProgressDTO.builder()
                    .subject(subject)
                    .totalAttempts(sTotal)
                    .correctAttempts(sCorrect)
                    .accuracy(sAccuracy)
                    .topicWiseProgress(topicProgressList)
                    .build());
        }

        return GateProgressDTO.builder()
                .totalAttempts(totalAttempts)
                .correctAttempts(correctAttempts)
                .incorrectAttempts(incorrectAttempts)
                .accuracy(accuracy)
                .averageScore(averageScore)
                .strongestSubject(strongestSubject)
                .weakestSubject(weakestSubject)
                .subjectWiseProgress(subjectProgressList)
                .build();
    }
}
