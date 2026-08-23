package com.vaibhav.aiinterviewcoach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaibhav.aiinterviewcoach.coding.dto.CodingProblemDTO;
import com.vaibhav.aiinterviewcoach.coding.dto.CodingSubmissionRequest;
import com.vaibhav.aiinterviewcoach.coding.dto.HintResponse;
import com.vaibhav.aiinterviewcoach.coding.entity.CodingProblem;
import com.vaibhav.aiinterviewcoach.coding.enums.Difficulty;
import com.vaibhav.aiinterviewcoach.coding.repository.CodingProblemRepository;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.progress.entity.Skill;
import com.vaibhav.aiinterviewcoach.progress.enums.SkillCategory;
import com.vaibhav.aiinterviewcoach.progress.repository.SkillRepository;
import com.vaibhav.aiinterviewcoach.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-m7")
public class CodingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private CodingProblemRepository problemRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser;
    
    @BeforeEach
    public void setup() {
        if (userRepository.findByEmail("test@example.com").isEmpty()) {
            User user = new User();
            user.setName("Test User");
            user.setEmail("test@example.com");
            user.setPassword("password");
            user.setRole("ROLE_USER");
            testUser = userRepository.save(user);
        } else {
            testUser = userRepository.findByEmail("test@example.com").get();
        }

        if (skillRepository.findByName("ARRAYS").isEmpty()) {
            Skill skill = Skill.builder()
                .name("ARRAYS")
                .category(SkillCategory.DSA)
                .build();
            skillRepository.save(skill);
        }
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGenerateProblemAndSubmit() throws Exception {
        // 1. Generate Problem
        String generateRequest = "{\"topic\":\"ARRAYS\",\"difficulty\":\"MEDIUM\"}";
        MvcResult genResult = mockMvc.perform(post("/api/v1/coding/problems/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateRequest))
                .andExpect(status().isOk())
                .andReturn();
                
        CodingProblemDTO problem = objectMapper.readValue(genResult.getResponse().getContentAsString(), CodingProblemDTO.class);
        assertNotNull(problem.getId());
        assertEquals("Two Sum", problem.getTitle());
        
        // 2. Hint
        mockMvc.perform(post("/api/v1/coding/problems/" + problem.getId() + "/hint"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hint").exists());

        // 3. Submit
        CodingSubmissionRequest req = new CodingSubmissionRequest();
        req.setCode("class Solution {}");
        
        mockMvc.perform(post("/api/v1/coding/problems/" + problem.getId() + "/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.score").value(100));
                
        // 4. Progress
        mockMvc.perform(get("/api/v1/coding/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptedProblems").value(1))
                .andExpect(jsonPath("$.solvedProblems").value(1))
                .andExpect(jsonPath("$.averageScore").value(100.0));
    }
}
