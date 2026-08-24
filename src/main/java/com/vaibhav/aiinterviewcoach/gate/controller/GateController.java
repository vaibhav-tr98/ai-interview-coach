package com.vaibhav.aiinterviewcoach.gate.controller;

import com.vaibhav.aiinterviewcoach.gate.dto.*;
import com.vaibhav.aiinterviewcoach.gate.enums.GateDifficulty;
import com.vaibhav.aiinterviewcoach.gate.enums.GateQuestionType;
import com.vaibhav.aiinterviewcoach.gate.service.GateAttemptService;
import com.vaibhav.aiinterviewcoach.gate.service.GateProgressService;
import com.vaibhav.aiinterviewcoach.gate.service.GateQuestionService;
import com.vaibhav.aiinterviewcoach.gate.service.GateSubjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gate")
public class GateController {

    private final GateSubjectService subjectService;
    private final GateQuestionService questionService;
    private final GateAttemptService attemptService;
    private final GateProgressService progressService;

    public GateController(GateSubjectService subjectService, GateQuestionService questionService,
                          GateAttemptService attemptService, GateProgressService progressService) {
        this.subjectService = subjectService;
        this.questionService = questionService;
        this.attemptService = attemptService;
        this.progressService = progressService;
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<String>> getSubjects() {
        return ResponseEntity.ok(subjectService.getSubjects());
    }

    @GetMapping("/subjects/{subject}/topics")
    public ResponseEntity<List<String>> getTopics(@PathVariable String subject) {
        return ResponseEntity.ok(subjectService.getTopics(subject));
    }

    @PostMapping("/questions/generate")
    public ResponseEntity<GateQuestionDTO> generateQuestion(@RequestBody GateQuestionRequest request) {
        return ResponseEntity.ok(questionService.generateQuestion(request));
    }

    @GetMapping("/questions")
    public ResponseEntity<List<GateQuestionDTO>> getQuestions(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) GateDifficulty difficulty,
            @RequestParam(required = false) GateQuestionType type) {
        return ResponseEntity.ok(questionService.getQuestions(subject, topic, difficulty, type));
    }

    @GetMapping("/questions/{questionId}")
    public ResponseEntity<GateQuestionDTO> getQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(questionService.getQuestionById(questionId));
    }

    @PostMapping("/questions/{questionId}/attempt")
    public ResponseEntity<GateAttemptResponse> attemptQuestion(@PathVariable Long questionId, @RequestBody GateAttemptRequest request) {
        return ResponseEntity.ok(attemptService.submitAttempt(questionId, request));
    }

    @PostMapping("/questions/{questionId}/explain")
    public ResponseEntity<GateExplanationResponse> explainQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(questionService.explainQuestion(questionId));
    }

    @PostMapping("/questions/{questionId}/hint")
    public ResponseEntity<GateHintResponse> hintQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(questionService.hintQuestion(questionId));
    }

    @GetMapping("/progress")
    public ResponseEntity<GateProgressDTO> getProgress() {
        return ResponseEntity.ok(progressService.getProgress());
    }

    @GetMapping("/practice")
    public ResponseEntity<List<GateQuestionDTO>> getPractice(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) GateDifficulty difficulty,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(questionService.getPracticeQuestions(subject, topic, difficulty, limit));
    }
}
