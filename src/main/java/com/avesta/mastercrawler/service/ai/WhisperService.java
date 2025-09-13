package com.avesta.mastercrawler.service.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class WhisperService {

    private final ChatLanguageModel chatLanguageModel;
    private final String apiKey;

    @Autowired
    public WhisperService(ChatLanguageModel chatLanguageModel, @Value("${whisper.api.key}") String apiKey) {
        this.chatLanguageModel = chatLanguageModel;
        this.apiKey = apiKey;
    }

    public String convertToText(String fileUrl) {
        try {
            File audioFile = new File(fileUrl);

            HttpResponse<String> response = Unirest.post("https://api.avalai.ir/v1/audio/transcriptions")
                    .header("Authorization", "Bearer " + apiKey)
                    .field("model", "whisper-1")
                    .field("response_format", "text")
                    .field("file", audioFile)
                    .connectTimeout(300000)
                    .socketTimeout(300000)
                    .asString();

            if (response.getStatus() == 200) {
                JSONObject jsonResponse = new JSONObject(response.getBody());

                String transcribedText = jsonResponse.getString("text");

                String prompt = "غلط املایی این متن را درست کن و جمله بندی آن را درست کن و آن را پاراگراف بندی کن و فقط متن اصلاح شده را بده هیچ چیز اضافه ای نگو .و سعی کن متن اصلی را تا جایی که ممکن است عوض نکنی. "+transcribedText;
                String textResponse = chatLanguageModel.chat(prompt);
                return textResponse;
            } else {
                System.err.println("Error: " + response.getBody());
                return response.getBody();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
