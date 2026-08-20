package com.vaibhav.aiinterviewcoach.analytics;

import com.vaibhav.aiinterviewcoach.analytics.dto.*;
import com.vaibhav.aiinterviewcoach.analytics.service.AnalyticsService;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.intelligence.entity.JobDescription;
import com.vaibhav.aiinterviewcoach.intelligence.entity.Resume;
import com.vaibhav.aiinterviewcoach.intelligence.repository.JobDescriptionRepository;
import com.vaibhav.aiinterviewcoach.intelligence.repository.ResumeRepository;
import com.vaibhav.aiinterviewcoach.intelligence.service.ResumeJobMatchService;
import com.vaibhav.aiinterviewcoach.interview.entity.AnswerEvaluation;
import com.vaibhav.aiinterviewcoach.interview.entity.Interview;
import com.vaibhav.aiinterviewcoach.interview.entity.QuestionAnswer;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewStatus;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewType;
import com.vaibhav.aiinterviewcoach.interview.repository.AnswerEvaluationRepository;
import com.vaibhav.aiinterviewcoach.interview.repository.InterviewRepository;
import com.vaibhav.aiinterviewcoach.interview.repository.QuestionAnswerRepository;
import com.vaibhav.aiinterviewcoach.progress.entity.UserSkillProgress;
import com.vaibhav.aiinterviewcoach.progress.repository.UserSkillProgressRepository;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyticsServiceTest {

    @Mock private InterviewRepository interviewRepository;
    @Mock private QuestionAnswerRepository questionAnswerRepository;
    @Mock private AnswerEvaluationRepository answerEvaluationRepository;
    @Mock private UserSkillProgressRepository userSkillProgressRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private JobDescriptionRepository jobDescriptionRepository;
    @Mock private ResumeJobMatchService resumeJobMatchService;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks private AnalyticsService analyticsService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@test.com");
        
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
    }

    @Test
    void testEmptyState() {
        when(interviewRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(answerEvaluationRepository.findByQuestionAnswer_Session_Interview_UserId(1L)).thenReturn(Collections.emptyList());
        when(userSkillProgressRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        
        AnalyticsOverviewResponse response = analyticsService.getOverview();
        
        assertEquals(0, response.totalInterviews());
        assertEquals(0, response.completedInterviews());
        assertEquals(0.0, response.averageInterviewScore());
        assertNull(response.latestInterviewScore());
    }

    @Test
    void testGetInterviewHistory() {
        Interview i1 = new Interview();
        i1.setId(101L);
        i1.setType(InterviewType.JAVA);
        i1.setStatus(InterviewStatus.COMPLETED);
        
        when(interviewRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(i1));
        when(answerEvaluationRepository.findByQuestionAnswer_Session_Interview_UserId(1L)).thenReturn(Collections.emptyList());
        when(questionAnswerRepository.findBySession_Interview_UserId(1L)).thenReturn(Collections.emptyList());

        List<InterviewHistoryDTO> result = analyticsService.getInterviewHistory();
        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).interviewId());
        assertEquals("JAVA", result.get(0).interviewType());
    }

}
