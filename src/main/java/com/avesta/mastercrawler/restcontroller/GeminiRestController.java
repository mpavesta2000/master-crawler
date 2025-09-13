package com.avesta.mastercrawler.restcontroller;


import com.avesta.mastercrawler.service.ai.GeminiService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class GeminiRestController {

    private final GeminiService geminiService;

    @PostMapping("/gemini/modify")
    public Map<String, String> geminiModify(@RequestBody Map<String, String> request) {
        if (request == null || request.isEmpty()) {
            throw new IllegalArgumentException("Request cannot be null or empty");
        }

        Set<String> responseKeys = Set.of("title", "body", "lead", "headline", "subTitle");

        for (Map.Entry<String, String> entry : request.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!responseKeys.contains(key)) {
                return Map.of("content", geminiService.getResponseCms(value, key));
            }
        }

        throw new IllegalArgumentException("Invalid prompt type in the request");
    }

}
