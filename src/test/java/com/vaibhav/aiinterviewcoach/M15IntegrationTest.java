package com.vaibhav.aiinterviewcoach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaibhav.aiinterviewcoach.ai.AiService;
import com.vaibhav.aiinterviewcoach.english.dto.*;
import com.vaibhav.aiinterviewcoach.english.entity.EnglishPracticeSession;
import com.vaibhav.aiinterviewcoach.english.repository.EnglishPracticeSessionRepository;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-m7")
public class M15IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EnglishPracticeSessionRepository sessionRepository;

    @SpyBean
    private AiService aiService;

    private String tokenA;
    private String tokenB;
    private User userA;
    private User userB;

    @BeforeEach
    void setUp() throws Exception {
        String emailA = "m15_usera_" + System.currentTimeMillis() + "@test.com";
        String emailB = "m15_userb_" + System.currentTimeMillis() + "@test.com";

        userA = new User();
        userA.setEmail(emailA);
        userA.setPassword(passwordEncoder.encode("password"));
        userA.setName("User A");
        userA.setRole("USER");
        userA = userRepository.save(userA);

        userB = new User();
        userB.setEmail(emailB);
        userB.setPassword(passwordEncoder.encode("password"));
        userB.setName("User B");
        userB.setRole("USER");
        userB = userRepository.save(userB);

        tokenA = getJwtToken(emailA, "password");
        tokenB = getJwtToken(emailB, "password");
    }

    private String getJwtToken(String email, String password) throws Exception {
        String authJson = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        MvcResult res = mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authJson))
                .andExpect(status().isOk())
                .andReturn();
        String json = res.getResponse().getContentAsString();
        return objectMapper.readTree(json).get("token").asText();
    }

    @Test
    void testM15_EnglishPracticeFlow() throws Exception {
        // 1. Fresh progress A
        MvcResult progressRes1 = mockMvc.perform(get("/api/v1/english/progress")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andReturn();
        
        EnglishPracticeProgressResponse prog1 = objectMapper.readValue(
                progressRes1.getResponse().getContentAsString(),
                EnglishPracticeProgressResponse.class
        );
        assertEquals(0, prog1.getTotalSessions());
        assertEquals(0, prog1.getCompletedSessions());
        assertEquals(0.0, prog1.getAverageOverallScore());
        
        // 2. Create Session (User A)
        EnglishPracticeSessionRequest createReq = EnglishPracticeSessionRequest.builder()
                .participantOneRole("Customer")
                .participantTwoRole("Support Agent")
                .topic("Billing Issue")
                .build();
                
        MvcResult createRes = mockMvc.perform(post("/api/v1/english/sessions")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();
                
        EnglishPracticeSessionResponse sessionCreated = objectMapper.readValue(
                createRes.getResponse().getContentAsString(),
                EnglishPracticeSessionResponse.class
        );
        Long sessionId = sessionCreated.getId();
        assertNotNull(sessionId);
        
        // 3. User B cannot access User A's session
        mockMvc.perform(get("/api/v1/english/sessions/" + sessionId)
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
                
        // 4. Add Messages (User A)
        EnglishPracticeMessageRequest msg1 = EnglishPracticeMessageRequest.builder()
                .participantRole("Customer")
                .messageText("Hi, I was charged twice this month.")
                .build();
                
        mockMvc.perform(post("/api/v1/english/sessions/" + sessionId + "/messages")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(msg1)))
                .andExpect(status().isCreated());
                
        EnglishPracticeMessageRequest msg2 = EnglishPracticeMessageRequest.builder()
                .participantRole("Support Agent")
                .messageText("I'm sorry to hear that. Let me check your account.")
                .build();
                
        mockMvc.perform(post("/api/v1/english/sessions/" + sessionId + "/messages")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(msg2)))
                .andExpect(status().isCreated());
                
        // User B cannot add message to A's session
        mockMvc.perform(post("/api/v1/english/sessions/" + sessionId + "/messages")
                .header("Authorization", "Bearer " + tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(msg2)))
                .andExpect(status().isForbidden());
                
        // 5. Get Session
        MvcResult getRes = mockMvc.perform(get("/api/v1/english/sessions/" + sessionId)
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andReturn();
                
        EnglishPracticeSessionDetailResponse detailRes = objectMapper.readValue(
                getRes.getResponse().getContentAsString(),
                EnglishPracticeSessionDetailResponse.class
        );
        assertEquals(2, detailRes.getMessages().size());
        assertEquals("Customer", detailRes.getMessages().get(0).getParticipantRole());
        assertEquals("Support Agent", detailRes.getMessages().get(1).getParticipantRole());
        assertNull(detailRes.getEvaluation());
        
        // 6. Evaluate Session
        long aiCallCountBefore = countAiCalls();
        
        MvcResult evalRes = mockMvc.perform(post("/api/v1/english/sessions/" + sessionId + "/evaluate")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andReturn();
                
        long aiCallCountAfter = countAiCalls();
        assertEquals(1, aiCallCountAfter - aiCallCountBefore, "Exactly 1 AI call expected");
        
        EnglishPracticeEvaluationResponse evalResponse = objectMapper.readValue(
                evalRes.getResponse().getContentAsString(),
                EnglishPracticeEvaluationResponse.class
        );
        assertEquals(81, evalResponse.getOverallScore()); // from mock
        
        // Check session status is completed
        EnglishPracticeSession dbSession = sessionRepository.findById(sessionId).orElseThrow();
        assertEquals("COMPLETED", dbSession.getStatus().name());
        
        // 7. Evaluate Again (Should be 0 AI calls)
        aiCallCountBefore = countAiCalls();
        mockMvc.perform(post("/api/v1/english/sessions/" + sessionId + "/evaluate")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());
        aiCallCountAfter = countAiCalls();
        assertEquals(0, aiCallCountAfter - aiCallCountBefore, "0 AI calls expected on re-evaluate");
        
        // 8. User B cannot evaluate User A session
        mockMvc.perform(post("/api/v1/english/sessions/" + sessionId + "/evaluate")
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
                
        // 9. Progress updated for A
        MvcResult progressRes2 = mockMvc.perform(get("/api/v1/english/progress")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andReturn();
                
        EnglishPracticeProgressResponse prog2 = objectMapper.readValue(
                progressRes2.getResponse().getContentAsString(),
                EnglishPracticeProgressResponse.class
        );
        assertEquals(1, prog2.getTotalSessions());
        assertEquals(1, prog2.getCompletedSessions());
        assertEquals(81.0, prog2.getAverageOverallScore());
        
        // User B progress remains empty
        MvcResult progressResB = mockMvc.perform(get("/api/v1/english/progress")
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andReturn();
                
        EnglishPracticeProgressResponse progB = objectMapper.readValue(
                progressResB.getResponse().getContentAsString(),
                EnglishPracticeProgressResponse.class
        );
        assertEquals(0, progB.getTotalSessions());
        assertEquals(0, progB.getCompletedSessions());
    }

    private long countAiCalls() {
        // Spying on aiService to count askGemini calls
        // In Mockito, we can't easily retrieve the global count if it's mixed with other tests.
        // We will just verify it via Mockitoing. But let's write a small state variable if needed.
        // Actually, Mockito.mockingDetails(aiService).getInvocations().stream().filter(...) is better.
        return org.mockito.Mockito.mockingDetails(aiService)
                .getInvocations()
                .stream()
                .filter(inv -> inv.getMethod().getName().equals("askGemini"))
                .count();
    }
}
