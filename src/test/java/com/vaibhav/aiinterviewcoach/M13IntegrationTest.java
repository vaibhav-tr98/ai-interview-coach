package com.vaibhav.aiinterviewcoach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaibhav.aiinterviewcoach.deepinterview.dto.DeepInterviewAnswerRequest;
import com.vaibhav.aiinterviewcoach.entity.User;
import com.vaibhav.aiinterviewcoach.project.dto.ProjectRequest;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-m7")
public class M13IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        if (userRepository.findByEmail("testm13@example.com").isEmpty()) {
            User user = new User();
            user.setName("Test M13 User");
            user.setEmail("testm13@example.com");
            user.setPassword("password");
            user.setRole("ROLE_USER");
            userRepository.save(user);
        }
    }

    @Test
    @WithMockUser(username = "testm13@example.com")
    public void testProjectDeepInterviewLifecycle() throws Exception {
        // 1. Create Project
        ProjectRequest prjReq = new ProjectRequest("My Project", "Desc", "Java", "http://github.com/myprj");
        MvcResult prjRes = mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prjReq)))
                .andExpect(status().isOk())
                .andReturn();
        
        com.vaibhav.aiinterviewcoach.project.dto.ProjectResponse prj = 
            objectMapper.readValue(prjRes.getResponse().getContentAsString(), com.vaibhav.aiinterviewcoach.project.dto.ProjectResponse.class);

        // 2. Start Deep Interview
        MvcResult startRes = mockMvc.perform(post("/api/v1/deep-interview/project/" + prj.getId() + "/start"))
                .andExpect(status().isOk())
                .andReturn();
                
        com.vaibhav.aiinterviewcoach.deepinterview.dto.DeepInterviewStartResponse start = 
            objectMapper.readValue(startRes.getResponse().getContentAsString(), com.vaibhav.aiinterviewcoach.deepinterview.dto.DeepInterviewStartResponse.class);
            
        assertNotNull(start.sessionId());

        // 3. Answer questions until complete (5 questions)
        for (int i = 0; i < 5; i++) {
            DeepInterviewAnswerRequest ansReq = new DeepInterviewAnswerRequest("My answer " + i);
            MvcResult ansRes = mockMvc.perform(post("/api/v1/deep-interview/" + start.sessionId() + "/answer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(ansReq)))
                    .andExpect(status().isOk())
                    .andReturn();
                    
            com.vaibhav.aiinterviewcoach.deepinterview.dto.DeepInterviewAnswerResponse ans = 
                objectMapper.readValue(ansRes.getResponse().getContentAsString(), com.vaibhav.aiinterviewcoach.deepinterview.dto.DeepInterviewAnswerResponse.class);
                
            if (i < 4) {
                assertFalse(ans.isComplete());
                assertNotNull(ans.nextQuestion());
            } else {
                assertTrue(ans.isComplete());
            }
        }

        // 4. Get Final Result
        MvcResult finalRes = mockMvc.perform(get("/api/v1/deep-interview/" + start.sessionId() + "/result"))
                .andExpect(status().isOk())
                .andReturn();
                
        com.vaibhav.aiinterviewcoach.deepinterview.dto.DeepInterviewResultResponse result = 
            objectMapper.readValue(finalRes.getResponse().getContentAsString(), com.vaibhav.aiinterviewcoach.deepinterview.dto.DeepInterviewResultResponse.class);
            
        assertEquals(5, result.totalQuestions());
        assertNotNull(result.projectOwnershipScore());
    }
}
