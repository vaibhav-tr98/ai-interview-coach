package com.vaibhav.aiinterviewcoach.coding.service;

import com.vaibhav.aiinterviewcoach.coding.dto.CodeExecutionResult;
import com.vaibhav.aiinterviewcoach.coding.dto.CodingSubmissionDTO;
import com.vaibhav.aiinterviewcoach.coding.dto.CodingSubmissionRequest;
import com.vaibhav.aiinterviewcoach.coding.entity.CodingProblem;
import com.vaibhav.aiinterviewcoach.coding.entity.CodingSubmission;
import com.vaibhav.aiinterviewcoach.coding.enums.SubmissionStatus;
import com.vaibhav.aiinterviewcoach.coding.repository.CodingProblemRepository;
import com.vaibhav.aiinterviewcoach.coding.repository.CodingSubmissionRepository;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.progress.entity.Skill;
import com.vaibhav.aiinterviewcoach.progress.entity.UserSkillProgress;
import com.vaibhav.aiinterviewcoach.progress.repository.UserSkillProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodingSubmissionService {

    private final CodingSubmissionRepository submissionRepository;
    private final CodingProblemRepository problemRepository;
    private final CodeExecutionService executionService;
    private final UserSkillProgressRepository userSkillProgressRepository;

    public CodingSubmissionDTO submitCode(User user, Long problemId, CodingSubmissionRequest request) {
        CodingProblem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));

        CodingSubmission submission = CodingSubmission.builder()
                .user(user)
                .problem(problem)
                .code(request.getCode())
                .language(request.getLanguage() != null ? request.getLanguage() : "JAVA")
                .status(SubmissionStatus.PENDING)
                .build();

        submission = submissionRepository.save(submission);

        CodeExecutionResult result = executionService.execute(submission.getCode(), problem);

        submission.setStatus(result.getStatus());
        submission.setScore(result.getScore());
        submission.setExecutionTimeMs(result.getExecutionTimeMs());
        submission.setMemoryUsedKb(result.getMemoryUsedKb());
        submission.setTestCaseResults(result.getTestCaseResults());

        submission = submissionRepository.save(submission);

        if (result.getStatus() == SubmissionStatus.ACCEPTED) {
            updateSkillProgress(user, problem.getSkill(), result.getScore());
        }

        return mapToDTO(submission);
    }

    private void updateSkillProgress(User user, Skill skill, Integer newScore) {
        UserSkillProgress progress = userSkillProgressRepository.findByUserIdAndSkillId(user.getId(), skill.getId())
                .orElse(UserSkillProgress.builder()
                        .user(user)
                        .skill(skill)
                        .averageScore(0.0)
                        .attemptCount(0)
                        .bestScore(0)
                        .weakestScore(100)
                        .build());

        int currentAttempts = progress.getAttemptCount();
        double currentAvg = progress.getAverageScore();
        
        double newAvg = ((currentAvg * currentAttempts) + newScore) / (currentAttempts + 1);
        
        progress.setAverageScore(newAvg);
        progress.setAttemptCount(currentAttempts + 1);
        
        if (newScore > progress.getBestScore()) {
            progress.setBestScore(newScore);
        }
        if (progress.getAttemptCount() == 1 || newScore < progress.getWeakestScore()) {
            progress.setWeakestScore(newScore);
        }
        
        progress.setLastPracticedAt(LocalDateTime.now());
        
        userSkillProgressRepository.save(progress);
    }

    public List<CodingSubmissionDTO> getUserSubmissions(User user) {
        return submissionRepository.findByUserId(user.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CodingSubmissionDTO getSubmission(User user, Long submissionId) {
        CodingSubmission submission = submissionRepository.findByIdAndUserId(submissionId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Submission not found or unauthorized"));
        return mapToDTO(submission);
    }

    private CodingSubmissionDTO mapToDTO(CodingSubmission s) {
        return CodingSubmissionDTO.builder()
                .id(s.getId())
                .problemId(s.getProblem().getId())
                .problemTitle(s.getProblem().getTitle())
                .code(s.getCode())
                .language(s.getLanguage())
                .status(s.getStatus())
                .score(s.getScore())
                .executionTimeMs(s.getExecutionTimeMs())
                .memoryUsedKb(s.getMemoryUsedKb())
                .testCaseResults(s.getTestCaseResults())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
