package com.vaibhav.aiinterviewcoach.dashboard.service;

import com.vaibhav.aiinterviewcoach.coding.dto.CodingProgressDTO;
import com.vaibhav.aiinterviewcoach.coding.entity.CodingSubmission;
import com.vaibhav.aiinterviewcoach.coding.repository.CodingSubmissionRepository;
import com.vaibhav.aiinterviewcoach.coding.service.CodingProgressService;
import com.vaibhav.aiinterviewcoach.communication.dto.CommunicationOverviewResponse;
import com.vaibhav.aiinterviewcoach.communication.entity.CommunicationAssessment;
import com.vaibhav.aiinterviewcoach.communication.repository.CommunicationAssessmentRepository;
import com.vaibhav.aiinterviewcoach.communication.service.CommunicationAssessmentService;
import com.vaibhav.aiinterviewcoach.dashboard.dto.DashboardActivityDto;
import com.vaibhav.aiinterviewcoach.dashboard.dto.DashboardResponse;
import com.vaibhav.aiinterviewcoach.english.dto.EnglishPracticeProgressResponse;
import com.vaibhav.aiinterviewcoach.english.entity.EnglishPracticeSession;
import com.vaibhav.aiinterviewcoach.english.repository.EnglishPracticeSessionRepository;
import com.vaibhav.aiinterviewcoach.english.service.EnglishPracticeService;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.gate.dto.GateProgressDTO;
import com.vaibhav.aiinterviewcoach.gate.entity.GateAttempt;
import com.vaibhav.aiinterviewcoach.gate.repository.GateAttemptRepository;
import com.vaibhav.aiinterviewcoach.gate.service.GateProgressService;
import com.vaibhav.aiinterviewcoach.interview.entity.Interview;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewType;
import com.vaibhav.aiinterviewcoach.interview.repository.InterviewRepository;
import com.vaibhav.aiinterviewcoach.planner.dto.StudyPlanResponse;
import com.vaibhav.aiinterviewcoach.planner.dto.StudyTaskResponse;
import com.vaibhav.aiinterviewcoach.planner.service.StudyPlanService;
import com.vaibhav.aiinterviewcoach.progress.dto.OverallProgressResponse;
import com.vaibhav.aiinterviewcoach.progress.dto.SkillScoreDTO;
import com.vaibhav.aiinterviewcoach.progress.service.ProgressService;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final CodingProgressService codingProgressService;
    private final CodingSubmissionRepository codingSubmissionRepository;
    private final GateProgressService gateProgressService;
    private final GateAttemptRepository gateAttemptRepository;
    private final EnglishPracticeService englishPracticeService;
    private final EnglishPracticeSessionRepository englishPracticeSessionRepository;
    private final CommunicationAssessmentService communicationAssessmentService;
    private final CommunicationAssessmentRepository communicationAssessmentRepository;
    private final StudyPlanService studyPlanService;
    private final ProgressService progressService;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        User user = getAuthenticatedUser();

        // 1. Interviews
        List<Interview> allInterviews = interviewRepository.findByUser(user);
        int totalInterviews = allInterviews.size();
        int completedInterviews = (int) allInterviews.stream().filter(i -> i.getCompletedAt() != null).count();
        int deepInterviewCount = (int) allInterviews.stream()
                .filter(i -> i.getType() == InterviewType.PROJECT || i.getType() == InterviewType.RESUME)
                .count();

        // 2. Coding Progress
        CodingProgressDTO codingProgress = codingProgressService.getProgress(user);

        // 3. GATE Progress
        GateProgressDTO gateProgress = gateProgressService.getProgress();

        // 4. Communication Score
        CommunicationOverviewResponse commOverview = communicationAssessmentService.getOverview();

        // 5. English Practice
        EnglishPracticeProgressResponse englishProgress = englishPracticeService.getProgress();

        // 6. Study Plan
        StudyPlanResponse studyPlan = studyPlanService.getActivePlan(user.getEmail());
        double studyPlanProgress = 0.0;
        if (studyPlan != null && studyPlan.getTasks() != null && !studyPlan.getTasks().isEmpty()) {
            long completedTasks = studyPlan.getTasks().stream().filter(StudyTaskResponse::getCompleted).count();
            studyPlanProgress = ((double) completedTasks / studyPlan.getTasks().size()) * 100.0;
        }

        // 7. Overall M7 Progress (Strengths/Weaknesses/Recommendations)
        OverallProgressResponse m7Progress = progressService.getOverallProgress(user);
        List<String> strongestAreas = m7Progress.strongestSkills() != null ? 
                m7Progress.strongestSkills().stream().map(SkillScoreDTO::skill).collect(Collectors.toList()) : new ArrayList<>();
        List<String> weakestAreas = m7Progress.weakestSkills() != null ? 
                m7Progress.weakestSkills().stream().map(SkillScoreDTO::skill).collect(Collectors.toList()) : new ArrayList<>();
        
        String recommendedNextAction = "No recommendation available. Take an interview!";
        if (m7Progress.recommendedSkills() != null && !m7Progress.recommendedSkills().isEmpty()) {
            recommendedNextAction = m7Progress.recommendedSkills().get(0).reason();
        }

        // 8. Readiness Calculation
        double overallReadinessScore = calculateReadinessScore(
                codingProgress.getSuccessRate(),
                gateProgress.getAccuracy(),
                commOverview.getAverageOverallScore(),
                englishProgress.getAverageOverallScore(),
                studyPlanProgress,
                m7Progress.overallScore()
        );

        // 9. Recent Activity
        List<DashboardActivityDto> activities = getRecentActivity(user, allInterviews);

        return DashboardResponse.builder()
                .overallReadinessScore(overallReadinessScore)
                .totalInterviews(totalInterviews)
                .completedInterviews(completedInterviews)
                .codingAttempts(codingProgress.getAttemptedProblems())
                .codingSuccessRate(codingProgress.getSuccessRate())
                .gateAttempts(gateProgress.getTotalAttempts())
                .gateAccuracy(gateProgress.getAccuracy())
                .communicationScore(commOverview.getAverageOverallScore())
                .englishPracticeScore(englishProgress.getAverageOverallScore())
                .studyPlanProgress(studyPlanProgress)
                .deepInterviewCount(deepInterviewCount)
                .strongestAreas(strongestAreas)
                .weakestAreas(weakestAreas)
                .recentActivity(activities)
                .recommendedNextAction(recommendedNextAction)
                .build();
    }

    private double calculateReadinessScore(double codingSuccess, double gateAccuracy, double commScore, double englishScore, double studyProgress, int m7OverallScore) {
        double score = 0;
        int weight = 0;
        
        if (codingSuccess > 0) { score += codingSuccess; weight++; }
        if (gateAccuracy > 0) { score += gateAccuracy; weight++; }
        if (commScore > 0) { score += commScore; weight++; }
        if (englishScore > 0) { score += englishScore; weight++; }
        if (studyProgress > 0) { score += studyProgress; weight++; }
        if (m7OverallScore > 0) { score += m7OverallScore; weight++; }
        
        if (weight == 0) return 0.0;
        
        return Math.round((score / weight) * 10.0) / 10.0;
    }

    private List<DashboardActivityDto> getRecentActivity(User user, List<Interview> interviews) {
        List<DashboardActivityDto> activities = new ArrayList<>();

        // Interviews
        interviews.stream().filter(i -> i.getCompletedAt() != null).forEach(i -> {
            activities.add(new DashboardActivityDto(
                    "INTERVIEW_COMPLETED",
                    "Completed interview: " + i.getTitle(),
                    i.getCompletedAt()
            ));
        });

        // Coding
        List<CodingSubmission> codingSubmissions = codingSubmissionRepository.findByUserId(user.getId());
        codingSubmissions.forEach(c -> {
            activities.add(new DashboardActivityDto(
                    "CODING_SUBMISSION",
                    "Submitted solution for " + c.getProblem().getTitle(),
                    c.getCreatedAt()
            ));
        });

        // GATE
        List<GateAttempt> gateAttempts = gateAttemptRepository.findByUserId(user.getId());
        gateAttempts.forEach(g -> {
            activities.add(new DashboardActivityDto(
                    "GATE_ATTEMPT",
                    "Attempted GATE question on " + g.getQuestion().getTopic(),
                    g.getAttemptedAt()
            ));
        });

        // English Practice
        List<EnglishPracticeSession> englishSessions = englishPracticeSessionRepository.findByUserOrderByIdDesc(user);
        englishSessions.stream().filter(e -> e.getCompletedAt() != null).forEach(e -> {
            activities.add(new DashboardActivityDto(
                    "ENGLISH_PRACTICE",
                    "Completed English session: " + e.getTopic(),
                    e.getCompletedAt()
            ));
        });
        
        // Communication Assessments
        List<CommunicationAssessment> commAssessments = communicationAssessmentRepository.findAllByUserOrderByAssessedAtDesc(user);
        commAssessments.forEach(c -> {
            activities.add(new DashboardActivityDto(
                    "COMMUNICATION_ASSESSMENT",
                    "Communication evaluated for interview",
                    c.getAssessedAt()
            ));
        });

        // Sort by timestamp desc and take top 10
        return activities.stream()
                .filter(a -> a.getTimestamp() != null)
                .sorted(Comparator.comparing(DashboardActivityDto::getTimestamp).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
