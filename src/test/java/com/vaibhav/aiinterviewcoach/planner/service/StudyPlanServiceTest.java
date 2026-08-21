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
import com.vaibhav.aiinterviewcoach.progress.enums.SkillCategory;
import com.vaibhav.aiinterviewcoach.progress.repository.SkillRepository;
import com.vaibhav.aiinterviewcoach.progress.service.RecommendationService;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudyPlanServiceTest {

    @Mock
    private StudyPlanRepository studyPlanRepository;
    @Mock
    private StudyTaskRepository studyTaskRepository;
    @Mock
    private StudyPlanAiService studyPlanAiService;
    @Mock
    private RecommendationService recommendationService;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StudyPlanService studyPlanService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
    }

    @Test
    void testGeneratePlan_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        
        StudyPlan activePlan = new StudyPlan();
        activePlan.setStatus(PlanStatus.ACTIVE);
        when(studyPlanRepository.findAllByUserEmailAndStatus("test@example.com", PlanStatus.ACTIVE))
                .thenReturn(List.of(activePlan));

        when(recommendationService.getRecommendations(user, "Java Dev"))
                .thenReturn(List.of(new RecommendationDTO("JAVA", "HIGH", "Reason")));
        
        Skill skill = new Skill(1L, "JAVA", SkillCategory.JAVA, "Java", LocalDateTime.now());
        when(skillRepository.findAll()).thenReturn(List.of(skill));

        AiStudyPlanResponse aiResp = new AiStudyPlanResponse();
        aiResp.setTargetRole("Java Dev");
        AiStudyPlanResponse.AiStudyTask aiTask = new AiStudyPlanResponse.AiStudyTask();
        aiTask.setDayNumber(1);
        aiTask.setSkill("JAVA");
        aiTask.setTopic("Basics");
        aiTask.setDescription("Learn Basics");
        aiResp.setDays(List.of(aiTask));

        when(studyPlanAiService.generateStudyPlan(anyString(), anyString(), anyString())).thenReturn(aiResp);

        when(studyPlanRepository.save(any(StudyPlan.class))).thenAnswer(invocation -> {
            StudyPlan sp = invocation.getArgument(0);
            sp.setId(10L);
            if (sp.getTasks() != null && !sp.getTasks().isEmpty()) {
                sp.getTasks().get(0).setId(100L);
            }
            return sp;
        });

        StudyPlanResponse resp = studyPlanService.generatePlan("test@example.com", "Java Dev");
        
        assertNotNull(resp);
        assertEquals("Java Dev", resp.getTargetRole());
        assertEquals(PlanStatus.ACTIVE, resp.getStatus());
        assertEquals(1, resp.getTasks().size());
        assertEquals("JAVA", resp.getTasks().get(0).getSkillName());

        verify(studyPlanRepository).saveAll(argThat(iterable -> {
            StudyPlan p = iterable.iterator().next();
            return p.getStatus() == PlanStatus.ABANDONED;
        }));
    }

    @Test
    void testGetActivePlan() {
        StudyPlan plan = new StudyPlan();
        plan.setId(5L);
        plan.setStatus(PlanStatus.ACTIVE);
        when(studyPlanRepository.findByUserEmailAndStatus("test@example.com", PlanStatus.ACTIVE))
                .thenReturn(Optional.of(plan));

        StudyPlanResponse resp = studyPlanService.getActivePlan("test@example.com");
        assertNotNull(resp);
        assertEquals(5L, resp.getId());
    }

    @Test
    void testCompleteTask() {
        StudyPlan plan = new StudyPlan();
        plan.setStatus(PlanStatus.ACTIVE);
        plan.setUser(user);

        StudyTask task1 = new StudyTask();
        task1.setId(1L);
        task1.setCompleted(false);
        task1.setStudyPlan(plan);

        StudyTask task2 = new StudyTask();
        task2.setId(2L);
        task2.setCompleted(true);
        task2.setStudyPlan(plan);

        plan.setTasks(List.of(task1, task2));

        when(studyTaskRepository.findById(1L)).thenReturn(Optional.of(task1));

        StudyTaskResponse resp = studyPlanService.completeTask("test@example.com", 1L);
        assertTrue(resp.getCompleted());
        assertEquals(PlanStatus.COMPLETED, plan.getStatus()); // since both are now true

        verify(studyTaskRepository).save(task1);
        verify(studyPlanRepository).save(plan);
    }
    
    @Test
    void testCompleteTask_Unauthorized() {
        StudyPlan plan = new StudyPlan();
        User wrongUser = new User();
        wrongUser.setEmail("wrong@example.com");
        plan.setUser(wrongUser);

        StudyTask task = new StudyTask();
        task.setId(1L);
        task.setStudyPlan(plan);

        when(studyTaskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(RuntimeException.class, () -> studyPlanService.completeTask("test@example.com", 1L));
    }
}
