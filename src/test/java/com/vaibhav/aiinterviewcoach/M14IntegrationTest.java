package com.vaibhav.aiinterviewcoach;

import com.vaibhav.aiinterviewcoach.gate.dto.*;
import com.vaibhav.aiinterviewcoach.gate.enums.GateDifficulty;
import com.vaibhav.aiinterviewcoach.gate.enums.GateQuestionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-m7")
public class M14IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String userToken;
    private String user2Token;

    @BeforeEach
    void setUp() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String e1 = "gate1_" + suffix + "@test.com";
        String e2 = "gate2_" + suffix + "@test.com";
        // Register and login User 1
        Map<String, String> regReq = Map.of("name", "GateUser1", "email", e1, "password", "pass123", "role", "USER");
        restTemplate.postForEntity("/api/v1/users/register", regReq, Map.class);
        Map<String, String> loginReq = Map.of("email", e1, "password", "pass123");
        ResponseEntity<Map> loginRes = restTemplate.postForEntity("/api/v1/users/login", loginReq, Map.class);
        userToken = (String) loginRes.getBody().get("token");

        // Register and login User 2
        Map<String, String> regReq2 = Map.of("name", "GateUser2", "email", e2, "password", "pass123", "role", "USER");
        restTemplate.postForEntity("/api/v1/users/register", regReq2, Map.class);
        Map<String, String> loginReq2 = Map.of("email", e2, "password", "pass123");
        ResponseEntity<Map> loginRes2 = restTemplate.postForEntity("/api/v1/users/login", loginReq2, Map.class);
        user2Token = (String) loginRes2.getBody().get("token");
    }

    private HttpHeaders getHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void testGateEngineLifecycle() {
        HttpHeaders headers = getHeaders(userToken);
        
        // 1. Get Subjects
        ResponseEntity<List> subjectsRes = restTemplate.exchange("/api/v1/gate/subjects", HttpMethod.GET, new HttpEntity<>(headers), List.class);
        assertThat(subjectsRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(subjectsRes.getBody()).contains("Algorithms");

        // 2. Get Topics
        ResponseEntity<List> topicsRes = restTemplate.exchange("/api/v1/gate/subjects/Algorithms/topics", HttpMethod.GET, new HttpEntity<>(headers), List.class);
        assertThat(topicsRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(topicsRes.getBody()).contains("Sorting");

        // 3. Generate Question
        GateQuestionRequest genReq = new GateQuestionRequest("Algorithms", "Sorting", GateDifficulty.MEDIUM, GateQuestionType.MCQ);
        ResponseEntity<GateQuestionDTO> genRes = restTemplate.exchange("/api/v1/gate/questions/generate", HttpMethod.POST, new HttpEntity<>(genReq, headers), GateQuestionDTO.class);
        assertThat(genRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        GateQuestionDTO question = genRes.getBody();
        assertThat(question).isNotNull();
        assertThat(question.getQuestionText()).contains("time complexity");
        
        // Note: correctOption should be null/hidden in generation response DTO
        // Actually, we modified it to map conditionally, but we passed false in generation! Let's check logic:
        // mapToDTO(question, false); so it should be null.
        assertThat(question.getCorrectOption()).isNull();

        Long qId = question.getId();

        // 4. Retrieve Question
        ResponseEntity<GateQuestionDTO> getRes = restTemplate.exchange("/api/v1/gate/questions/" + qId, HttpMethod.GET, new HttpEntity<>(headers), GateQuestionDTO.class);
        assertThat(getRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getRes.getBody().getCorrectOption()).isNull();

        // 5. Submit Attempt (Incorrect)
        GateAttemptRequest attReqInc = new GateAttemptRequest("B");
        ResponseEntity<GateAttemptResponse> attResInc = restTemplate.exchange("/api/v1/gate/questions/" + qId + "/attempt", HttpMethod.POST, new HttpEntity<>(attReqInc, headers), GateAttemptResponse.class);
        assertThat(attResInc.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(attResInc.getBody().getIsCorrect()).isFalse();
        assertThat(attResInc.getBody().getScore()).isEqualTo(0);

        // 6. Submit Attempt (Correct)
        GateAttemptRequest attReqCor = new GateAttemptRequest("C");
        ResponseEntity<GateAttemptResponse> attResCor = restTemplate.exchange("/api/v1/gate/questions/" + qId + "/attempt", HttpMethod.POST, new HttpEntity<>(attReqCor, headers), GateAttemptResponse.class);
        assertThat(attResCor.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(attResCor.getBody().getIsCorrect()).isTrue();
        assertThat(attResCor.getBody().getScore()).isEqualTo(100);

        // 7. Hint
        ResponseEntity<GateHintResponse> hintRes = restTemplate.exchange("/api/v1/gate/questions/" + qId + "/hint", HttpMethod.POST, new HttpEntity<>(null, headers), GateHintResponse.class);
        assertThat(hintRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(hintRes.getBody().getHint()).isNotNull();

        // 8. Explain
        ResponseEntity<GateExplanationResponse> explainRes = restTemplate.exchange("/api/v1/gate/questions/" + qId + "/explain", HttpMethod.POST, new HttpEntity<>(null, headers), GateExplanationResponse.class);
        assertThat(explainRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(explainRes.getBody().getExplanation()).isNotNull();

        // 9. Get Progress
        ResponseEntity<GateProgressDTO> progRes = restTemplate.exchange("/api/v1/gate/progress", HttpMethod.GET, new HttpEntity<>(headers), GateProgressDTO.class);
        assertThat(progRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        GateProgressDTO progress = progRes.getBody();
        assertThat(progress.getTotalAttempts()).isEqualTo(2); // 1 wrong, 1 right
        assertThat(progress.getAccuracy()).isEqualTo(50.0);
        
        // 10. Check Isolation for User 2 (User 2 has 0 attempts)
        HttpHeaders headers2 = getHeaders(user2Token);
        ResponseEntity<GateProgressDTO> progRes2 = restTemplate.exchange("/api/v1/gate/progress", HttpMethod.GET, new HttpEntity<>(headers2), GateProgressDTO.class);
        assertThat(progRes2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(progRes2.getBody().getTotalAttempts()).isEqualTo(0);
    }
}
