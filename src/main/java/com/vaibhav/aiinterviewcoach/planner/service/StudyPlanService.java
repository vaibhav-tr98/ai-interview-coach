package com.vaibhav.aiinterviewcoach.planner.service;

import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.planner.dto.AiStudyPlanResponse;
import com.vaibhav.aiinterviewcoach.planner.dto.StudyPlanResponse;
import com.vaibhav.aiinterviewcoach.planner.dto.StudyTaskResponse;
import com.vaibhav.aiinterviewcoach.planner.entity.StudyPlan;
import com.vaibhav.aiinterviewcoach.planner.entity.StudyTask;
import com.vaibhav.aiinterviewcoach.planner.enums.PlanStatus;
import com.vaibhav.aiinterviewcoach.planner.repository.StudyPlanRepository;
import com.vaibhav.aiinterviewcoach.planner.repository.StudyTaskRepository;
import com.vaibhav.aiinterviewcoach.progress.dto.RecommendationDTO;
import com.vaibhav.aiinterviewcoach.progress.entity.Skill;
import com.vaibhav.aiinterviewcoach.progress.repository.SkillRepository;
import com.vaibhav.aiinterviewcoach.progress.service.RecommendationService;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final StudyTaskRepository studyTaskRepository;
    private final StudyPlanAiService studyPlanAiService;
    private final RecommendationService recommendationService;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    @Transactional
    public StudyPlanResponse generatePlan(String email, String targetRole) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Abandon existing active plans
        List<StudyPlan> activePlans = studyPlanRepository.findAllByUserEmailAndStatus(email, PlanStatus.ACTIVE);
        for (StudyPlan plan : activePlans) {
            plan.setStatus(PlanStatus.ABANDONED);
        }
        studyPlanRepository.saveAll(activePlans);

        List<RecommendationDTO> recommendations = recommendationService.getRecommendations(user, targetRole);
        String weaknessesStr = recommendations.stream()
                .map(r -> r.skill() + " (Priority: " + r.priority() + " - " + r.reason() + ")")
                .collect(Collectors.joining("\n"));
                
        if (weaknessesStr.isBlank()) {
            weaknessesStr = "The user has no recorded weaknesses. Provide a foundational study plan for the target role.";
        }

        List<Skill> allSkills = skillRepository.findAll();
        Set<String> allowedSkillNames = allSkills.stream().map(Skill::getName).collect(Collectors.toSet());
        String allowedSkillsStr = String.join(", ", allowedSkillNames);

        AiStudyPlanResponse aiPlan = studyPlanAiService.generateStudyPlan(targetRole, weaknessesStr, allowedSkillsStr);

        StudyPlan plan = StudyPlan.builder()
                .user(user)
                .targetRole(aiPlan.getTargetRole() != null ? aiPlan.getTargetRole() : targetRole)
                .status(PlanStatus.ACTIVE)
                .build();

        if (aiPlan.getDays() != null) {
            for (AiStudyPlanResponse.AiStudyTask aiTask : aiPlan.getDays()) {
                // Ignore tasks with skills that don't exist in canonical list
                if (aiTask.getSkill() != null && allowedSkillNames.contains(aiTask.getSkill().toUpperCase())) {
                    StudyTask task = StudyTask.builder()
                            .studyPlan(plan)
                            .dayNumber(aiTask.getDayNumber())
                            .skillName(aiTask.getSkill().toUpperCase())
                            .topic(aiTask.getTopic())
                            .description(aiTask.getDescription())
                            .completed(false)
                            .build();
                    plan.getTasks().add(task);
                }
            }
        }

        StudyPlan savedPlan = studyPlanRepository.save(plan);
        return mapToResponse(savedPlan);
    }

    @Transactional(readOnly = true)
    public StudyPlanResponse getActivePlan(String email) {
        return studyPlanRepository.findByUserEmailAndStatus(email, PlanStatus.ACTIVE)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Transactional
    public StudyTaskResponse completeTask(String email, Long taskId) {
        StudyTask task = studyTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getStudyPlan().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized: This task does not belong to the user");
        }

        task.setCompleted(true);
        studyTaskRepository.save(task);

        StudyPlan plan = task.getStudyPlan();
        boolean allCompleted = plan.getTasks().stream().allMatch(StudyTask::getCompleted);
        if (allCompleted) {
            plan.setStatus(PlanStatus.COMPLETED);
            studyPlanRepository.save(plan);
        }

        return mapToTaskResponse(task);
    }

    private StudyPlanResponse mapToResponse(StudyPlan plan) {
        return StudyPlanResponse.builder()
                .id(plan.getId())
                .targetRole(plan.getTargetRole())
                .status(plan.getStatus())
                .createdAt(plan.getCreatedAt())
                .tasks(plan.getTasks().stream().map(this::mapToTaskResponse).collect(Collectors.toList()))
                .build();
    }

    private StudyTaskResponse mapToTaskResponse(StudyTask task) {
        return StudyTaskResponse.builder()
                .id(task.getId())
                .dayNumber(task.getDayNumber())
                .skillName(task.getSkillName())
                .topic(task.getTopic())
                .description(task.getDescription())
                .completed(task.getCompleted())
                .build();
    }
}
