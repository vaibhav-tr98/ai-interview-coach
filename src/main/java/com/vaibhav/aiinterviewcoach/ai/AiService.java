package com.vaibhav.aiinterviewcoach.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient chatClient;

    public AiService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String askGemini(String prompt) {

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}