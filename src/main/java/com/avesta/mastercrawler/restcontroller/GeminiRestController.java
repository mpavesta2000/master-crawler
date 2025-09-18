package com.avesta.mastercrawler.restcontroller;


import com.avesta.mastercrawler.service.ai.GeminiService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/gemini/generate-slug")
    public ResponseEntity<Map<String, String>> generateSlug(@RequestBody Map<String, String> request) {
        try {
            // Get required parameters
            String persianKeyphrase = request.get("keyphrase");
            String title = request.get("title");
            String body = request.get("body");

            // Validate input
            if (persianKeyphrase == null || persianKeyphrase.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "کلیدواژه اصلی الزامی است"));
            }

            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "عنوان خبر الزامی است"));
            }

            if (body == null || body.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "محتوای خبر الزامی است"));
            }

            // Generate AI slug
            String generatedSlug = geminiService.generateSlug(persianKeyphrase.trim(), title.trim(), body.trim());

            return ResponseEntity.ok(Map.of(
                "slug", generatedSlug,
                "message", "اسلاگ با موفقیت تولید شد"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "خطایی در تولید اسلاگ رخ داد: " + e.getMessage()
            ));
        }
    }

}
