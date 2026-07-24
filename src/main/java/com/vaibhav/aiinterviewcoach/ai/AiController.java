package com.vaibhav.aiinterviewcoach.ai;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public PromptResponse chat(
            @RequestBody @Valid PromptRequest request
    ) {

        String response = aiService.askGemini(request.prompt());

        return new PromptResponse(response);
    }
}