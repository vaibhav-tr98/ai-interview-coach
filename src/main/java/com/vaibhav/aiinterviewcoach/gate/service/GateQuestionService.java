package com.vaibhav.aiinterviewcoach.gate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaibhav.aiinterviewcoach.gate.dto.GateExplanationResponse;
import com.vaibhav.aiinterviewcoach.gate.dto.GateHintResponse;
import com.vaibhav.aiinterviewcoach.gate.dto.GateQuestionDTO;
import com.vaibhav.aiinterviewcoach.gate.dto.GateQuestionRequest;
import com.vaibhav.aiinterviewcoach.gate.entity.GateQuestion;
import com.vaibhav.aiinterviewcoach.gate.enums.GateDifficulty;
import com.vaibhav.aiinterviewcoach.gate.enums.GateQuestionType;
import com.vaibhav.aiinterviewcoach.gate.repository.GateQuestionRepository;
import com.vaibhav.aiinterviewcoach.progress.entity.Skill;
import com.vaibhav.aiinterviewcoach.progress.enums.SkillCategory;
import com.vaibhav.aiinterviewcoach.progress.repository.SkillRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GateQuestionService {

    private final GateQuestionRepository questionRepository;
    private final GateSubjectService subjectService;
    private final SkillRepository skillRepository;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public GateQuestionService(GateQuestionRepository questionRepository, GateSubjectService subjectService,
                               SkillRepository skillRepository, ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.questionRepository = questionRepository;
        this.subjectService = subjectService;
        this.skillRepository = skillRepository;
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public GateQuestionDTO generateQuestion(GateQuestionRequest request) {
        if (!subjectService.isValidSubject(request.getSubject())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid subject");
        }

        String prompt = "Generate a technical question for GATE preparation.\n" +
                "Subject: " + request.getSubject() + "\n" +
                "Topic: " + (request.getTopic() != null ? request.getTopic() : "Any topic") + "\n" +
                "Difficulty: " + (request.getDifficulty() != null ? request.getDifficulty() : "MEDIUM") + "\n" +
                "Type: " + (request.getQuestionType() != null ? request.getQuestionType() : "MCQ") + "\n" +
                "Provide the response STRICTLY as a JSON object with NO markdown formatting, with exactly these fields:\n" +
                "- questionText (string)\n" +
                "- optionA (string)\n" +
                "- optionB (string)\n" +
                "- optionC (string)\n" +
                "- optionD (string)\n" +
                "- correctOption (string, exactly A, B, C, or D)\n" +
                "- explanation (string)";

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // Clean markdown backticks if any exist
        response = response.replaceAll("```json", "").replaceAll("```", "").trim();

        try {
            GateQuestion aiGen = objectMapper.readValue(response, GateQuestion.class);

            Skill skill = skillRepository.findByName(request.getSubject())
                    .orElseGet(() -> skillRepository.save(Skill.builder()
                            .name(request.getSubject())
                            .category(SkillCategory.GENERAL)
                            .description(request.getSubject() + " preparation")
                            .build()));

            GateQuestion question = GateQuestion.builder()
                    .skill(skill)
                    .topic(request.getTopic() != null ? request.getTopic() : "General")
                    .questionText(aiGen.getQuestionText())
                    .questionType(request.getQuestionType() != null ? request.getQuestionType() : GateQuestionType.MCQ)
                    .difficulty(request.getDifficulty() != null ? request.getDifficulty() : GateDifficulty.MEDIUM)
                    .optionA(aiGen.getOptionA())
                    .optionB(aiGen.getOptionB())
                    .optionC(aiGen.getOptionC())
                    .optionD(aiGen.getOptionD())
                    .correctOption(aiGen.getCorrectOption())
                    .explanation(aiGen.getExplanation())
                    .build();

            question = questionRepository.save(question);
            return mapToDTO(question, false);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse AI response: " + e.getMessage());
        }
    }

    public List<GateQuestionDTO> getQuestions(String subject, String topic, GateDifficulty difficulty, GateQuestionType type) {
        List<GateQuestion> questions = questionRepository.findQuestions(subject, topic, difficulty, type);
        return questions.stream().map(q -> mapToDTO(q, false)).collect(Collectors.toList());
    }

    public GateQuestionDTO getQuestionById(Long id) {
        GateQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        return mapToDTO(question, false);
    }

    public List<GateQuestionDTO> getPracticeQuestions(String subject, String topic, GateDifficulty difficulty, int limit) {
        List<GateQuestion> questions = questionRepository.findRandomQuestions(subject, topic, 
                difficulty != null ? difficulty.name() : null, limit);
        return questions.stream().map(q -> mapToDTO(q, false)).collect(Collectors.toList());
    }

    public GateExplanationResponse explainQuestion(Long questionId) {
        GateQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

        if (question.getExplanation() != null && !question.getExplanation().isBlank()) {
            return GateExplanationResponse.builder()
                    .questionId(question.getId())
                    .correctOption(question.getCorrectOption())
                    .explanation(question.getExplanation())
                    .build();
        }

        String prompt = "Explain the correct answer for this GATE question:\n" +
                "Question: " + question.getQuestionText() + "\n" +
                "Correct Option: " + question.getCorrectOption() + "\n" +
                "Respond STRICTLY with a JSON object containing exactly one field:\n" +
                "- explanation (string)";

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        response = response.replaceAll("```json", "").replaceAll("```", "").trim();

        try {
            GateExplanationResponse explainRes = objectMapper.readValue(response, GateExplanationResponse.class);
            
            question.setExplanation(explainRes.getExplanation());
            questionRepository.save(question);
            
            return GateExplanationResponse.builder()
                    .questionId(question.getId())
                    .correctOption(question.getCorrectOption())
                    .explanation(explainRes.getExplanation())
                    .build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse AI response: " + e.getMessage());
        }
    }

    public GateHintResponse hintQuestion(Long questionId) {
        GateQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

        String prompt = "Provide a short, helpful hint for this GATE question without revealing the answer:\n" +
                "Question: " + question.getQuestionText() + "\n" +
                "Respond STRICTLY with a JSON object containing exactly one field:\n" +
                "- hint (string)";

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        response = response.replaceAll("```json", "").replaceAll("```", "").trim();

        try {
            GateHintResponse hintRes = objectMapper.readValue(response, GateHintResponse.class);
            return GateHintResponse.builder()
                    .questionId(question.getId())
                    .hint(hintRes.getHint())
                    .build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse AI response: " + e.getMessage());
        }
    }

    public GateQuestionDTO mapToDTO(GateQuestion question, boolean includeAnswer) {
        GateQuestionDTO dto = GateQuestionDTO.builder()
                .id(question.getId())
                .subject(question.getSkill().getName())
                .topic(question.getTopic())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .difficulty(question.getDifficulty())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                .build();
                
        if (includeAnswer) {
            dto.setCorrectOption(question.getCorrectOption());
            dto.setExplanation(question.getExplanation());
        }
        return dto;
    }
}
