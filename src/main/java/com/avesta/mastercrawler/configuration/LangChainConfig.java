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

    @Value("${spring.ai.openai.chat.options.model}")
    private String apiModel;

    /** Overridable via `gemini.api.max-output-tokens` in application.properties. */
    @Value("${gemini.api.max-output-tokens:4096}")
    private Integer maxOutputTokens;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key must not be null or empty");
        }
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(apiUrl)
                .defaultRequestParameters(ChatRequestParameters.builder()
                        .modelName(apiModel)
                        .temperature(0.7)
                        // Without this no max_tokens is sent and the provider's
                        // default applies, which cut generated news articles off
                        // mid-sentence. This is an upper bound, not a fixed spend:
                        // short answers (tags, meta, slug, alt-text) still stop
                        // on their own.
                        .maxOutputTokens(maxOutputTokens)
                        .build())
                .build();
    }
}
