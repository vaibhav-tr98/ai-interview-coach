package com.vaibhav.aiinterviewcoach.communication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaibhav.aiinterviewcoach.ai.AiService;
import com.vaibhav.aiinterviewcoach.communication.dto.CommunicationAiResponse;
import com.vaibhav.aiinterviewcoach.communication.dto.CommunicationAssessmentResponse;
import com.vaibhav.aiinterviewcoach.communication.dto.CommunicationOverviewResponse;
import com.vaibhav.aiinterviewcoach.communication.entity.CommunicationAssessment;
import com.vaibhav.aiinterviewcoach.communication.repository.CommunicationAssessmentRepository;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.interview.entity.Interview;
import com.vaibhav.aiinterviewcoach.interview.entity.QuestionAnswer;
import com.vaibhav.aiinterviewcoach.interview.session.InterviewSession;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewStatus;
import com.vaibhav.aiinterviewcoach.interview.repository.InterviewRepository;
import com.vaibhav.aiinterviewcoach.interview.repository.QuestionAnswerRepository;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CommunicationAssessmentServiceTest {

    @Mock private CommunicationAssessmentRepository assessmentRepository;
    @Mock private InterviewRepository interviewRepository;
    @Mock private QuestionAnswerRepository questionAnswerRepository;
    @Mock private UserRepository userRepository;
    @Mock private CommunicationPromptBuilder promptBuilder;
    @Mock private AiService aiService;
    @Mock private ObjectMapper objectMapper;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks private CommunicationAssessmentService assessmentService;

    private User mockUser;
    private Interview mockInterview;
    private InterviewSession mockSession;

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

        mockInterview = new Interview();
        mockInterview.setId(10L);
        mockInterview.setUser(mockUser);
        mockInterview.setStatus(InterviewStatus.COMPLETED);

        mockSession = new InterviewSession();
        mockSession.setId(20L);
        mockSession.setInterview(mockInterview);
    }

    @Test
    void testAssessInterview_Success() throws Exception {
        when(interviewRepository.findById(10L)).thenReturn(Optional.of(mockInterview));
        when(assessmentRepository.existsByInterviewId(10L)).thenReturn(false);

        QuestionAnswer qa1 = new QuestionAnswer();
        qa1.setSession(mockSession);
        qa1.setQuestion("Tell me about yourself.");
        qa1.setAnswer("I am a software engineer.");

        when(questionAnswerRepository.findBySession_Interview_UserId(1L)).thenReturn(List.of(qa1));
        when(promptBuilder.buildAssessmentPrompt(any(), any())).thenReturn("Mock Prompt");
        when(aiService.askGemini("Mock Prompt")).thenReturn("```json\n{\"overallScore\": 85}\n```");

        CommunicationAiResponse mockAiResponse = new CommunicationAiResponse();
        mockAiResponse.setOverallScore(85);
        mockAiResponse.setClarityScore(80);
        when(objectMapper.readValue(anyString(), eq(CommunicationAiResponse.class))).thenReturn(mockAiResponse);

        CommunicationAssessment mockAssessment = new CommunicationAssessment();
        mockAssessment.setId(100L);
        mockAssessment.setInterview(mockInterview);
        mockAssessment.setOverallScore(85);
        mockAssessment.setClarityScore(80);
        mockAssessment.setAssessedAt(LocalDateTime.now());
        when(assessmentRepository.save(any(CommunicationAssessment.class))).thenReturn(mockAssessment);

        CommunicationAssessmentResponse response = assessmentService.assessInterview(10L);

        assertNotNull(response);
        assertEquals(85, response.getOverallScore());
        verify(aiService, times(1)).askGemini(anyString());
    }

    @Test
    void testAssessInterview_AlreadyExists() {
        when(interviewRepository.findById(10L)).thenReturn(Optional.of(mockInterview));
        when(assessmentRepository.existsByInterviewId(10L)).thenReturn(true);
        
        CommunicationAssessment mockAssessment = new CommunicationAssessment();
        mockAssessment.setId(100L);
        mockAssessment.setInterview(mockInterview);
        mockAssessment.setOverallScore(90);
        when(assessmentRepository.findByInterviewIdAndUser(10L, mockUser)).thenReturn(Optional.of(mockAssessment));

        CommunicationAssessmentResponse response = assessmentService.assessInterview(10L);

        assertNotNull(response);
        assertEquals(90, response.getOverallScore());
        verify(aiService, never()).askGemini(anyString()); // Zero AI calls if it exists
    }

    @Test
    void testAssessInterview_Unauthorized() {
        User otherUser = new User();
        otherUser.setId(2L);
        mockInterview.setUser(otherUser);
        when(interviewRepository.findById(10L)).thenReturn(Optional.of(mockInterview));

        assertThrows(AccessDeniedException.class, () -> assessmentService.assessInterview(10L));
    }

    @Test
    void testAssessInterview_Incomplete() {
        mockInterview.setStatus(InterviewStatus.STARTED);
        when(interviewRepository.findById(10L)).thenReturn(Optional.of(mockInterview));

        assertThrows(IllegalStateException.class, () -> assessmentService.assessInterview(10L));
    }

    @Test
    void testOverview_Empty() {
        when(assessmentRepository.findAllByUserOrderByAssessedAtDesc(mockUser)).thenReturn(Collections.emptyList());

        CommunicationOverviewResponse response = assessmentService.getOverview();

        assertNotNull(response);
        assertEquals(0, response.getTotalAssessments());
        assertEquals(0.0, response.getAverageOverallScore());
    }
}
