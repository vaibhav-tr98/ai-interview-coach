package com.vaibhav.aiinterviewcoach.analytics.service;

import com.vaibhav.aiinterviewcoach.analytics.dto.*;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.intelligence.dto.ResumeJobMatchResponse;
import com.vaibhav.aiinterviewcoach.intelligence.entity.JobDescription;
import com.vaibhav.aiinterviewcoach.intelligence.entity.Resume;
import com.vaibhav.aiinterviewcoach.intelligence.repository.JobDescriptionRepository;
import com.vaibhav.aiinterviewcoach.intelligence.repository.ResumeRepository;
import com.vaibhav.aiinterviewcoach.intelligence.service.ResumeJobMatchService;
import com.vaibhav.aiinterviewcoach.interview.entity.AnswerEvaluation;
import com.vaibhav.aiinterviewcoach.interview.entity.Interview;
import com.vaibhav.aiinterviewcoach.interview.entity.QuestionAnswer;
import com.vaibhav.aiinterviewcoach.interview.enums.InterviewStatus;
import com.vaibhav.aiinterviewcoach.interview.repository.AnswerEvaluationRepository;
import com.vaibhav.aiinterviewcoach.interview.repository.InterviewRepository;
import com.vaibhav.aiinterviewcoach.interview.repository.QuestionAnswerRepository;
import com.vaibhav.aiinterviewcoach.progress.entity.UserSkillProgress;
import com.vaibhav.aiinterviewcoach.progress.repository.UserSkillProgressRepository;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final InterviewRepository interviewRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final AnswerEvaluationRepository answerEvaluationRepository;
    private final UserSkillProgressRepository userSkillProgressRepository;
    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeJobMatchService resumeJobMatchService;
    private final UserRepository userRepository;

    public AnalyticsService(InterviewRepository interviewRepository,
                            QuestionAnswerRepository questionAnswerRepository,
                            AnswerEvaluationRepository answerEvaluationRepository,
                            UserSkillProgressRepository userSkillProgressRepository,
                            ResumeRepository resumeRepository,
                            JobDescriptionRepository jobDescriptionRepository,
                            ResumeJobMatchService resumeJobMatchService,
                            UserRepository userRepository) {
        this.interviewRepository = interviewRepository;
        this.questionAnswerRepository = questionAnswerRepository;
        this.answerEvaluationRepository = answerEvaluationRepository;
        this.userSkillProgressRepository = userSkillProgressRepository;
        this.resumeRepository = resumeRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.resumeJobMatchService = resumeJobMatchService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<InterviewHistoryDTO> getInterviewHistory() {
        User user = getAuthenticatedUser();
        List<Interview> interviews = interviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        
        List<AnswerEvaluation> allEvals = answerEvaluationRepository.findByQuestionAnswer_Session_Interview_UserId(user.getId());
        Map<Long, List<AnswerEvaluation>> evalsByInterview = allEvals.stream()
            .collect(Collectors.groupingBy(e -> e.getQuestionAnswer().getSession().getInterview().getId()));

        List<QuestionAnswer> allQas = questionAnswerRepository.findBySession_Interview_UserId(user.getId());
        Map<Long, Long> qasCountByInterview = allQas.stream()
            .collect(Collectors.groupingBy(qa -> qa.getSession().getInterview().getId(), Collectors.counting()));

        return interviews.stream().map(i -> {
            Integer overallScore = null;
            List<AnswerEvaluation> iEvals = evalsByInterview.getOrDefault(i.getId(), Collections.emptyList());
            if (i.getStatus() == InterviewStatus.COMPLETED && !iEvals.isEmpty()) {
                double total = iEvals.stream().mapToInt(AnswerEvaluation::getScore).sum();
                overallScore = (int) Math.round(total / iEvals.size());
            }

            int totalQ = qasCountByInterview.getOrDefault(i.getId(), 0L).intValue();
            
            return new InterviewHistoryDTO(
                i.getId(),
                i.getType().name(),
                i.getRole(),
                i.getStatus().name(),
                totalQ,
                overallScore,
                i.getCreatedAt(),
                i.getCompletedAt(),
                null, 
                null
            );
        }).toList();
    }

    public AnalyticsOverviewResponse getOverview() {
        User user = getAuthenticatedUser();
        List<Interview> interviews = interviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<UserSkillProgress> skillProgress = userSkillProgressRepository.findByUserId(user.getId());
        List<AnswerEvaluation> evals = answerEvaluationRepository.findByQuestionAnswer_Session_Interview_UserId(user.getId());

        long totalInterviews = interviews.size();
        long completedInterviews = interviews.stream().filter(i -> i.getStatus() == InterviewStatus.COMPLETED).count();
        long totalQuestionsAnswered = evals.size();

        Map<Long, List<AnswerEvaluation>> evalsByInterview = evals.stream()
            .collect(Collectors.groupingBy(e -> e.getQuestionAnswer().getSession().getInterview().getId()));

        List<Integer> completedScores = interviews.stream()
            .filter(i -> i.getStatus() == InterviewStatus.COMPLETED)
            .map(i -> {
                List<AnswerEvaluation> ie = evalsByInterview.getOrDefault(i.getId(), Collections.emptyList());
                if (ie.isEmpty()) return null;
                return (int) Math.round(ie.stream().mapToInt(AnswerEvaluation::getScore).average().orElse(0));
            })
            .filter(Objects::nonNull)
            .toList();

        double avgScore = completedScores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        Integer bestScore = completedScores.stream().max(Integer::compareTo).orElse(0);
        Integer latestScore = completedScores.isEmpty() ? null : completedScores.get(0); 

        String strongestSkill = skillProgress.stream().max(Comparator.comparing(UserSkillProgress::getAverageScore)).map(s -> s.getSkill().getName()).orElse(null);
        String weakestSkill = skillProgress.stream().min(Comparator.comparing(UserSkillProgress::getAverageScore)).map(s -> s.getSkill().getName()).orElse(null);

        List<String> improving = new ArrayList<>();
        List<String> declining = new ArrayList<>();
        List<String> stable = new ArrayList<>();
        
        for (UserSkillProgress p : skillProgress) {
            if (p.getAverageScore() >= 80) improving.add(p.getSkill().getName());
            else if (p.getAverageScore() < 50) declining.add(p.getSkill().getName());
            else stable.add(p.getSkill().getName());
        }

        String recentTrend = "STABLE";
        if (completedScores.size() >= 2) {
            if (completedScores.get(0) > completedScores.get(1)) recentTrend = "IMPROVING";
            else if (completedScores.get(0) < completedScores.get(1)) recentTrend = "DECLINING";
        }

        Map<String, Long> typeBreakdown = interviews.stream()
            .collect(Collectors.groupingBy(i -> i.getType().name(), Collectors.counting()));

        return new AnalyticsOverviewResponse(
            totalInterviews,
            completedInterviews,
            avgScore,
            bestScore,
            latestScore,
            totalQuestionsAnswered,
            strongestSkill,
            weakestSkill,
            improving,
            declining,
            stable,
            recentTrend,
            typeBreakdown
        );
    }

    public List<ScoreTrendDTO> getScoreTrend() {
        User user = getAuthenticatedUser();
        List<Interview> interviews = interviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<AnswerEvaluation> allEvals = answerEvaluationRepository.findByQuestionAnswer_Session_Interview_UserId(user.getId());
        Map<Long, List<AnswerEvaluation>> evalsByInterview = allEvals.stream()
            .collect(Collectors.groupingBy(e -> e.getQuestionAnswer().getSession().getInterview().getId()));

        return interviews.stream()
            .filter(i -> i.getStatus() == InterviewStatus.COMPLETED)
            .sorted(Comparator.comparing(Interview::getCreatedAt)) // Chronological
            .map(i -> {
                List<AnswerEvaluation> iEvals = evalsByInterview.getOrDefault(i.getId(), Collections.emptyList());
                if (iEvals.isEmpty()) return null;
                int score = (int) Math.round(iEvals.stream().mapToInt(AnswerEvaluation::getScore).average().orElse(0));
                return new ScoreTrendDTO(i.getId(), i.getType().name(), score, i.getCompletedAt());
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public Map<String, InterviewTypeAnalyticsDTO> getByType() {
        User user = getAuthenticatedUser();
        List<Interview> interviews = interviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<AnswerEvaluation> allEvals = answerEvaluationRepository.findByQuestionAnswer_Session_Interview_UserId(user.getId());
        Map<Long, List<AnswerEvaluation>> evalsByInterview = allEvals.stream()
            .collect(Collectors.groupingBy(e -> e.getQuestionAnswer().getSession().getInterview().getId()));

        Map<String, List<Interview>> grouped = interviews.stream().collect(Collectors.groupingBy(i -> i.getType().name()));

        Map<String, InterviewTypeAnalyticsDTO> result = new HashMap<>();
        for (var entry : grouped.entrySet()) {
            String type = entry.getKey();
            List<Interview> typeInterviews = entry.getValue();
            long attempts = typeInterviews.size();
            long completed = typeInterviews.stream().filter(i -> i.getStatus() == InterviewStatus.COMPLETED).count();

            List<Integer> scores = typeInterviews.stream()
                .filter(i -> i.getStatus() == InterviewStatus.COMPLETED)
                .map(i -> {
                    List<AnswerEvaluation> ie = evalsByInterview.getOrDefault(i.getId(), Collections.emptyList());
                    if (ie.isEmpty()) return null;
                    return (int) Math.round(ie.stream().mapToInt(AnswerEvaluation::getScore).average().orElse(0));
                })
                .filter(Objects::nonNull)
                .toList();
            
            double avg = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            Integer best = scores.stream().max(Integer::compareTo).orElse(0);
            Integer latest = scores.isEmpty() ? null : scores.get(0); 

            result.put(type, new InterviewTypeAnalyticsDTO(type, attempts, completed, avg, best, latest));
        }
        return result;
    }

    public List<SkillAnalyticsDTO> getSkills() {
        User user = getAuthenticatedUser();
        List<UserSkillProgress> progress = userSkillProgressRepository.findByUserId(user.getId());

        return progress.stream().map(p -> {
            double avg = p.getAverageScore();
            String strength = avg >= 80 ? "STRONG" : (avg >= 70 ? "GOOD" : (avg >= 50 ? "NEEDS_IMPROVEMENT" : "WEAK"));
            String trend = avg >= 70 ? "IMPROVING" : (avg < 50 ? "DECLINING" : "STABLE");

            return new SkillAnalyticsDTO(
                p.getSkill().getName(),
                p.getSkill().getCategory().name(),
                avg,
                p.getBestScore(),
                p.getWeakestScore(),
                p.getAttemptCount(),
                trend,
                p.getLastPracticedAt(),
                strength
            );
        }).toList();
    }

    public AnswerQualityResponse getAnswerQuality() {
        User user = getAuthenticatedUser();
        List<AnswerEvaluation> evals = answerEvaluationRepository.findByQuestionAnswer_Session_Interview_UserId(user.getId());

        if (evals.isEmpty()) {
            return new AnswerQualityResponse(0, 0, 0, 0, 0, 0, 0);
        }

        double avgScore = evals.stream().mapToInt(AnswerEvaluation::getScore).average().orElse(0);
        double avgLength = evals.stream().mapToInt(e -> e.getQuestionAnswer().getAnswer().length()).average().orElse(0);
        
        long strongCount = evals.stream().filter(e -> e.getScore() >= 80).count();
        long weakCount = evals.stream().filter(e -> e.getScore() < 50).count();

        double strongPct = (strongCount * 100.0) / evals.size();
        double weakPct = (weakCount * 100.0) / evals.size();

        int strongest = evals.stream().mapToInt(AnswerEvaluation::getScore).max().orElse(0);
        int weakest = evals.stream().mapToInt(AnswerEvaluation::getScore).min().orElse(0);

        return new AnswerQualityResponse(
            avgScore,
            avgLength,
            evals.size(),
            strongPct,
            weakPct,
            strongest,
            weakest
        );
    }

    public JobReadinessResponse getJobReadiness() {
        User user = getAuthenticatedUser();
        List<Resume> resumes = resumeRepository.findByUserId(user.getId());
        List<JobDescription> jds = jobDescriptionRepository.findByUserId(user.getId());

        if (resumes.isEmpty() || jds.isEmpty()) {
            return new JobReadinessResponse(null, false);
        }

        Resume latestResume = resumes.stream().max(Comparator.comparing(Resume::getCreatedAt)).get();
        JobDescription latestJd = jds.stream().max(Comparator.comparing(JobDescription::getCreatedAt)).get();

        try {
            ResumeJobMatchResponse match = resumeJobMatchService.matchResumeWithJd(latestResume.getId(), latestJd.getId());
            return new JobReadinessResponse(match, true);
        } catch (Exception e) {
            return new JobReadinessResponse(null, false);
        }
    }

    public List<RecentInterviewDTO> getRecent() {
        User user = getAuthenticatedUser();
        List<Interview> interviews = interviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<AnswerEvaluation> allEvals = answerEvaluationRepository.findByQuestionAnswer_Session_Interview_UserId(user.getId());
        Map<Long, List<AnswerEvaluation>> evalsByInterview = allEvals.stream()
            .collect(Collectors.groupingBy(e -> e.getQuestionAnswer().getSession().getInterview().getId()));

        return interviews.stream()
            .filter(i -> i.getStatus() == InterviewStatus.COMPLETED)
            .limit(5)
            .map(i -> {
                List<AnswerEvaluation> iEvals = evalsByInterview.getOrDefault(i.getId(), Collections.emptyList());
                if (iEvals.isEmpty()) return null;
                int score = (int) Math.round(iEvals.stream().mapToInt(AnswerEvaluation::getScore).average().orElse(0));
                
                String strongest = iEvals.stream().max(Comparator.comparing(AnswerEvaluation::getScore))
                        .map(AnswerEvaluation::getStrengths).orElse(null);
                String weakest = iEvals.stream().min(Comparator.comparing(AnswerEvaluation::getScore))
                        .map(AnswerEvaluation::getWeaknesses).orElse(null);

                return new RecentInterviewDTO(
                    i.getId(),
                    i.getType().name(),
                    score,
                    i.getCompletedAt(),
                    strongest,
                    weakest
                );
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
