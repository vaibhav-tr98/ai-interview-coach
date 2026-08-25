package com.vaibhav.aiinterviewcoach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaibhav.aiinterviewcoach.ai.AiService;
import com.vaibhav.aiinterviewcoach.dashboard.dto.DashboardResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-m7")
public class M16IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @SpyBean
    private AiService aiService;

    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() throws Exception {
        String emailA = "m16_usera_" + System.currentTimeMillis() + "@test.com";
        String emailB = "m16_userb_" + System.currentTimeMillis() + "@test.com";

        User userA = new User();
        userA.setEmail(emailA);
        userA.setPassword(passwordEncoder.encode("password"));
        userA.setName("User A");
        userA.setRole("USER");
        userRepository.save(userA);

        User userB = new User();
        userB.setEmail(emailB);
        userB.setPassword(passwordEncoder.encode("password"));
        userB.setName("User B");
        userB.setRole("USER");
        userRepository.save(userB);

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
    void testDashboardEmptyStateAndIsolation() throws Exception {
        long initialAiCalls = countAiCalls();

        // 1. User A (Empty State)
        MvcResult resA = mockMvc.perform(get("/api/v1/dashboard")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andReturn();

        DashboardResponse dashA = objectMapper.readValue(resA.getResponse().getContentAsString(), DashboardResponse.class);
        assertEquals(0.0, dashA.getOverallReadinessScore());
        assertEquals(0, dashA.getTotalInterviews());
        assertEquals(0, dashA.getCompletedInterviews());
        assertEquals(0, dashA.getCodingAttempts());
        assertEquals(0.0, dashA.getCodingSuccessRate());
        assertEquals(0, dashA.getGateAttempts());
        assertEquals(0.0, dashA.getGateAccuracy());
        assertEquals(0.0, dashA.getCommunicationScore());
        assertEquals(0.0, dashA.getEnglishPracticeScore());
        assertEquals(0.0, dashA.getStudyPlanProgress());
        assertEquals(0, dashA.getDeepInterviewCount());
        assertTrue(dashA.getStrongestAreas().isEmpty());
        assertTrue(dashA.getWeakestAreas().isEmpty());
        assertTrue(dashA.getRecentActivity().isEmpty());
        assertNotNull(dashA.getRecommendedNextAction());

        // 2. User B (Empty State)
        MvcResult resB = mockMvc.perform(get("/api/v1/dashboard")
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andReturn();

        DashboardResponse dashB = objectMapper.readValue(resB.getResponse().getContentAsString(), DashboardResponse.class);
        assertEquals(0, dashB.getTotalInterviews());

        // 3. AI Calls count should be 0
        long currentAiCalls = countAiCalls();
        assertEquals(0, currentAiCalls - initialAiCalls, "Dashboard MUST NOT make AI calls");
    }

    private long countAiCalls() {
        return org.mockito.Mockito.mockingDetails(aiService)
                .getInvocations()
                .stream()
                .filter(inv -> inv.getMethod().getName().equals("askGemini"))
                .count();
    }
}
