package com.avesta.mastercrawler.service.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news-generator")
@AllArgsConstructor
public class NewsGeneratorRestController {

    private final ChatLanguageModel chatLanguageModel;

    @GetMapping("/questions")
    public ResponseEntity<List<Map<String, Object>>> getQuestions(@RequestParam("modelId") String modelId) {
        List<Map<String, Object>> questions = new ArrayList<>();

        switch (modelId) {
            case "news-quote":
                questions.add(createQuestion("speaker", "خبر از زبان چه کسی گفته می‌شود؟", "text"));
                questions.add(createQuestion("subject", "خبر راجع به چه موضوعی است؟", "text"));
                questions.add(createQuestion("importance", "اهمیت این خبر در چیست؟", "text"));
                questions.add(createSelectQuestion("tone", "لحن خبر چگونه باشد؟",
                        List.of("رسمی", "غیررسمی", "علمی", "انتقادی", "خنثی")));
                break;

            case "news-event":
                questions.add(createQuestion("event", "چه رویدادی اتفاق افتاده است؟", "text"));
                questions.add(createQuestion("location", "محل وقوع رویداد کجاست؟", "text"));
                questions.add(createQuestion("time", "زمان وقوع رویداد چه بوده است؟", "text"));
                questions.add(createQuestion("participants", "افراد یا گروه‌های دخیل در رویداد چه کسانی هستند؟", "text"));
                questions.add(createQuestion("consequences", "پیامدهای این رویداد چیست؟", "text"));
                break;

            case "news-analysis":
                questions.add(createQuestion("topic", "موضوع تحلیل چیست؟", "text"));
                questions.add(createQuestion("background", "پیش‌زمینه و سابقه این موضوع چیست؟", "text"));
                questions.add(createQuestion("perspectives", "چه دیدگاه‌هایی درباره این موضوع وجود دارد؟", "text"));
                questions.add(createSelectQuestion("analysis_depth", "عمق تحلیل چقدر باشد؟",
                        List.of("سطحی", "متوسط", "عمیق")));
                questions.add(createQuestion("conclusion", "نتیجه‌گیری مدنظر شما چیست؟", "text"));
                break;
        }

        return ResponseEntity.ok(questions);
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateNews(@RequestBody Map<String, Object> requestBody) {
        String modelId = (String) requestBody.get("modelId");
        Map<String, String> answers = (Map<String, String>) requestBody.get("answers");

        String generatedNews = generateNewsWithAI(modelId, answers);

        Map<String, String> response = new HashMap<>();
        response.put("generatedNews", generatedNews);

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createQuestion(String id, String text, String type) {
        Map<String, Object> question = new HashMap<>();
        question.put("id", id);
        question.put("text", text);
        question.put("type", type);
        return question;
    }

    private Map<String, Object> createSelectQuestion(String id, String text, List<String> options) {
        Map<String, Object> question = createQuestion(id, text, "select");
        question.put("options", options);
        return question;
    }

    private String generateNewsWithAI(String modelId, Map<String, String> answers) {
        String prompt = constructPromptForModel(modelId, answers);

        try {
            return chatLanguageModel.chat(prompt);
        } catch (Exception e) {
            return "خطا در تولید خبر: " + e.getMessage();
        }
    }

    private String constructPromptForModel(String modelId, Map<String, String> answers) {
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("You are an expert news writer creating a professional news article in Persian. ");
        promptBuilder.append("Generate a coherent and engaging news piece based on the following details:\n\n");

        switch (modelId) {
            case "news-quote":
                promptBuilder.append("Create a quote-based news article with the following characteristics:\n");
                promptBuilder.append("- Speaker: ").append(answers.get("speaker")).append("\n");
                promptBuilder.append("- Subject: ").append(answers.get("subject")).append("\n");
                promptBuilder.append("- Importance: ").append(answers.get("importance")).append("\n");
                promptBuilder.append("- Tone: ").append(answers.get("tone")).append("\n");
                promptBuilder.append("\nWrite a professional news article that effectively quotes the speaker and provides context.");
                break;

            case "news-event":
                promptBuilder.append("Create an event-based news article with the following details:\n");
                promptBuilder.append("- Event: ").append(answers.get("event")).append("\n");
                promptBuilder.append("- Location: ").append(answers.get("location")).append("\n");
                promptBuilder.append("- Time: ").append(answers.get("time")).append("\n");
                promptBuilder.append("- Participants: ").append(answers.get("participants")).append("\n");
                promptBuilder.append("- Consequences: ").append(answers.get("consequences")).append("\n");
                promptBuilder.append("\nWrite a comprehensive news article that covers all aspects of the event.");
                break;

            case "news-analysis":
                promptBuilder.append("Create an analytical news article with the following components:\n");
                promptBuilder.append("- Topic: ").append(answers.get("topic")).append("\n");
                promptBuilder.append("- Background: ").append(answers.get("background")).append("\n");
                promptBuilder.append("- Perspectives: ").append(answers.get("perspectives")).append("\n");
                promptBuilder.append("- Analysis Depth: ").append(answers.get("analysis_depth")).append("\n");
                promptBuilder.append("- Conclusion: ").append(answers.get("conclusion")).append("\n");
                promptBuilder.append("\nWrite an in-depth analytical article that provides nuanced insights.");
                break;
        }

        promptBuilder.append("\n\nEnsure the article is:\n");
        promptBuilder.append("- Written in clear, professional Persian\n");
        promptBuilder.append("- Structured logically\n");
        promptBuilder.append("- Engaging and informative\n");
        promptBuilder.append("- Approximately 300-500 words long\n");
        promptBuilder.append("- And it must be seo based and seo friendly\n");
        promptBuilder.append("- And dont say anything more just give the answer\n");

        return promptBuilder.toString();
    }
}