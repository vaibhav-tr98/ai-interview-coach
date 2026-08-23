package com.vaibhav.aiinterviewcoach.coding.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaibhav.aiinterviewcoach.ai.AiService;
import com.vaibhav.aiinterviewcoach.coding.dto.CodingProblemDTO;
import com.vaibhav.aiinterviewcoach.coding.entity.CodingProblem;
import com.vaibhav.aiinterviewcoach.coding.enums.Difficulty;
import com.vaibhav.aiinterviewcoach.coding.repository.CodingProblemRepository;
import com.vaibhav.aiinterviewcoach.progress.entity.Skill;
import com.vaibhav.aiinterviewcoach.progress.enums.SkillCategory;
import com.vaibhav.aiinterviewcoach.progress.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodingProblemService {

    private final CodingProblemRepository problemRepository;
    private final SkillRepository skillRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public List<CodingProblemDTO> getProblems() {
        return problemRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CodingProblemDTO getProblem(Long id) {
        CodingProblem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));
        return mapToDTO(problem);
    }

    public CodingProblemDTO generateProblem(String topic, String difficultyLevel) {
        // Find existing canonical skill for the topic.
        String uppercaseTopic = topic.toUpperCase();
        Skill skill = skillRepository.findByName(uppercaseTopic)
                .orElseGet(() -> skillRepository.findByName("ARRAYS")
                        .orElseThrow(() -> new IllegalStateException("Canonical skills not found")));

        Difficulty difficulty;
        try {
            difficulty = Difficulty.valueOf(difficultyLevel.toUpperCase());
        } catch (Exception e) {
            difficulty = Difficulty.MEDIUM;
        }

        String prompt = "Generate a new coding problem for topic " + skill.getName() + " with difficulty " + difficulty + ". " +
                "Return exactly a JSON object with keys: title, description, constraints, examples, expectedInputFormat, expectedOutputFormat. " +
                "Do not include markdown blocks or any other text.";

        String aiResponse = aiService.askGemini(prompt);
        
        // Strip markdown backticks if present
        if (aiResponse.startsWith("```json")) {
            aiResponse = aiResponse.substring(7);
            if (aiResponse.endsWith("```")) {
                aiResponse = aiResponse.substring(0, aiResponse.length() - 3);
            }
        }

        try {
            JsonNode root = objectMapper.readTree(aiResponse);
            CodingProblem problem = CodingProblem.builder()
                    .title(root.path("title").asText())
                    .description(root.path("description").asText())
                    .difficulty(difficulty)
                    .skill(skill)
                    .constraints(root.path("constraints").asText())
                    .examples(root.path("examples").asText())
                    .expectedInputFormat(root.path("expectedInputFormat").asText())
                    .expectedOutputFormat(root.path("expectedOutputFormat").asText())
                    .build();

            problem = problemRepository.save(problem);
            return mapToDTO(problem);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate problem using AI", e);
        }
    }

    public String generateHint(Long problemId) {
        CodingProblem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));

        String prompt = "Give a brief hint for solving this problem without giving the exact code: " + problem.getTitle() + 
                "\n" + problem.getDescription();

        return aiService.askGemini(prompt);
    }

    private CodingProblemDTO mapToDTO(CodingProblem p) {
        return CodingProblemDTO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .difficulty(p.getDifficulty())
                .skillId(p.getSkill().getId())
                .skillName(p.getSkill().getName())
                .constraints(p.getConstraints())
                .examples(p.getExamples())
                .expectedInputFormat(p.getExpectedInputFormat())
                .expectedOutputFormat(p.getExpectedOutputFormat())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
