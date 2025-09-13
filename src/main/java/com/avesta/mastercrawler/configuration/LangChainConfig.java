package com.avesta.mastercrawler.configuration;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChainConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key must not be null or empty");
        }
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(apiUrl)
                .defaultRequestParameters(ChatRequestParameters.builder()
                        .modelName("gemini-2.0-flash")
                        .temperature(0.7)
                        .build())
                .build();
    }
}
