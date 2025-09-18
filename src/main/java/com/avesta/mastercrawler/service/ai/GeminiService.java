package com.avesta.mastercrawler.service.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class GeminiService {

    private final ChatLanguageModel chatLanguageModel;

    public String getResponseCms(String prompt, String promptType) {
        StringBuilder bodyPrompt = new StringBuilder();
        bodyPrompt.append("Rewrite the following Persian news body based on SEO best practices.\n");
        bodyPrompt.append("Follow these strict formatting rules:\n");
        bodyPrompt.append("1- ONLY return the final rewritten <body> content — no intros, summaries, or extra notes.\n");
        bodyPrompt.append("2- DO NOT remove or strip any existing HTML tags in the input.\n");
        bodyPrompt.append("3- DO NOT use <h1> tags at all.\n");
        bodyPrompt.append("4- Actively add on-page SEO structure:\n");
        bodyPrompt.append("   • Use <h2> for main section titles.\n");
        bodyPrompt.append("   • Use <h3> for subheadings inside sections.\n");
        bodyPrompt.append("   • Use <ul><li> to convert any lists or multi-point info.\n");
        bodyPrompt.append("   • Use <p> for paragraph structure (if not already present).\n");
        bodyPrompt.append("5- If the body does NOT already contain any <h2>, <h3>, or <ul><li> tags, you MUST add them logically based on the content flow.\n");
        bodyPrompt.append("6- The output must be in Farsi and visually structured using the tags above.\n");
        bodyPrompt.append("7- Wrap the whole answer in a div tag with id=answer.\n");
        bodyPrompt.append("8- Imagine this as the final HTML body of a published article.\n");


        StringBuilder tagPrompt = new StringBuilder();
        tagPrompt.append("Take this Persian news article and generate 10 meta keywords:\n");
        tagPrompt.append("1- The keywords must be highly relevant to the content of the article.\n");
        tagPrompt.append("2- The keywords must be in Persian only.\n");
        tagPrompt.append("3- Separate the keywords using English commas (,) — not Persian commas (،).\n");
        tagPrompt.append("4- Do not add any explanations or extra text.\n");
        tagPrompt.append("5- DONT wrap the answer in meta tag element");
        tagPrompt.append("6- Wrap the output in a <div> with id=answer. and nothing else");



        String extraPrompt = switch (promptType) {
            case "tag" -> tagPrompt.toString();
            case "metaTitle" -> "این متن را گرفته و از آن یک تیترمتا یا meta-title  خبری تولید کن و به من بده.   و جواب را در یک div قرار بده با id=answer و داخل دیو متن را داخل هیچ تگی نزار مثل h p  ";
            case "metaTag" -> "این متن را گرفته و از آن تگ های متا یا meta keywords برای خبر درست کن و به اینصورت به من بده.  مثلا سیاسی,اجتماعی    و جواب را در یک div قرار بده با id=answer ومهم است که keyword  ها با , جدا شود نه با ،";
            case "metaDescription" -> "این متن را گرفته و از آن یک متن متا یا meta-description  خبری تولید کن و به من بده.   و جواب را در یک div قرار بده  با id=answer ";
            case "bodySeo" -> bodyPrompt.toString();
            case "headlineSeo" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک روتیتر مناسب براساس استاندارد های seo برای خبر بده و این روتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با  id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "DescriptiveHeadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک روتیتر توضیحی مناسب براساس استاندارد های seo برای خبر بده و این روتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با  id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "SupplementaryHeadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک روتیتر تکمیلی مناسب براساس استاندارد های seo برای خبر بده و این روتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با  id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "NumericalHeadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک روتیتر عددی مناسب براساس استاندارد های seo برای خبر بده و این روتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با  id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "QuoteHeadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک روتیتر نقل قولی مناسب براساس استاندارد های seo برای خبر بده و این روتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با  id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "EmotionalHeadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک روتیتر حسی مناسب براساس استاندارد های seo برای خبر بده و این روتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با  id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "QuestionHeadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک روتیتر پرسشی مناسب براساس استاندارد های seo برای خبر بده و این روتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با  id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "ComparativeHeadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک روتیتر مقایسه‌ای مناسب براساس استاندارد های seo برای خبر بده و این روتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با  id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "AmazingHeadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک روتیتر شگفت‌انگیز مناسب براساس استاندارد های seo برای خبر بده و این روتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با  id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "subheadlineSeo" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک زیرتیتر مناسب براساس استاندارد های seo برای خبر بده و این زیرتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "DescriptiveSubheadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک زیرتیتر توضیحی مناسب براساس استاندارد های seo برای خبر بده  و این زیرتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "SupplementarySubheadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک زیرتیتر تکمیلی مناسب براساس استاندارد های seo برای خبر بده  و این زیرتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "NumericalSubheadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک زیرتیتر عددی مناسب براساس استاندارد های seo برای خبر بده  و این زیرتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "QuoteSubheadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک زیرتیتر نقل قولی مناسب براساس استاندارد های seo برای خبر بده  و این زیرتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "EmotionalSubheadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک زیرتیتر حسی مناسب براساس استاندارد های seo برای خبر بده  و این زیرتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "QuestionSubheadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک زیرتیتر پرسشی مناسب براساس استاندارد های seo برای خبر بده  و این زیرتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "ComparativeSubheadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک زیرتیتر مقایسه ای مناسب براساس استاندارد های seo برای خبر بده  و این زیرتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "AmazingSubheadline" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک زیرتیتر شگفت انگیز مناسب براساس استاندارد های seo برای خبر بده  و این زیرتیتر نباید طولانی باشد.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "AmazingTitle" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک تیتر شگفت انگیز مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "NumericalTitle" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک تیتر عددی مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "ComparativeTitle" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک تیتر مقایسه‌ای مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "QuestionTitle" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک تیتر سوالی مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "CatchyTitle" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک تیتر جذاب مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "DescriptiveTitle" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک تیتر توصیفی مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "NewsTitle" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک تیتر مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "DescriptiveLead" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک سرنخ توضیحی کوتاه مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "SupplementaryLead" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک سرنخ تکمیلی کوتاه مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "NumericalLead" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک سرنخ عددی کوتاه مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "QuoteLead" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک سرنخ نقل قولی کوتاه مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "EmotionalLead" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک سرنخ حسی کوتاه مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "QuestionLead" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک سرنخ پرسشی کوتاه مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "ComparativeLead" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک سرنخ مقایسه ای کوتاه مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "AmazingLead" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک سرنخ شگفت انگیز کوتاه مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            case "leadSeo" -> "این متن را گرفته و آن را بررسی کن و بر اساس آن به من یک سرنخ کوتاه مناسب براساس استاندارد های seo برای خبر بده.   و جواب را در یک div قرار بده با id=answer و فقط جواب را قرار بده، چیزی جز این نباشه ";
            default -> throw new IllegalArgumentException("Invalid prompt type: " + promptType);
        };

        prompt = prompt + " " + extraPrompt;

        String response = chatLanguageModel.chat(prompt);
        return response != null ? response : "No response from Gemini.";

    }

    public String generateResponse(Map<String, Object> generate) {

        String seo,keywords,setting,prompt,finalPrompt;

        setting = "آخرش کلمات کلیدی را نزار و جواب را در یک div قرار بده با id=answer ";
        keywords = (String) generate.get("keywords");
        seo = (String) generate.get("seo");
        prompt = (String) generate.get("generate");
        finalPrompt = prompt + "که این کلمات کلیدی در آن استفاده شده باشد:" + keywords + "و این موارد seo رعایت شود:"+ seo + setting;

        String response = chatLanguageModel.chat(finalPrompt);
        return response != null ? response : "No response from Gemini.";
    }

    public String modifyNewsParts(String prompt, String promptType) {
        String extraPrompt = switch (promptType) {
            case "tag" -> "این متن را گرفته و از آن تگ های خبری استخراج کن و به اینصورت به من بده.  مثلا سیاسی,اجتماعی  \n  و فقط جواب را بده چیز اضافه نده ومهم است که برچسب ها با , جدا شود نه با ،";
            case "metaTitle" -> "این متن را گرفته و از آن یک تیترمتا یا meta-title  خبری تولید کن و به من بده.  و فقط جواب را بده چیز اضافه نده ";
            case "metaTag" -> "این متن را گرفته و از آن تگ های متا یا meta keywords برای خبر درست کن و به اینصورت به من بده.  مثلا سیاسی,اجتماعی    و فقط جواب را بده چیز اضافه نده و که keyword  ها با , جدا شود نه با ،";
            case "metaDescription" -> "این متن را گرفته و از آن یک متن متا یا meta-description  خبری تولید کن و به من بده.  و فقط جواب را بده چیز اضافه نده ";
            case "body" -> "Get this news body and rewrite it based on seo\n And just return the answer dont add anything else\n And dont remove the tags in the body \n And dont use h1 tag in the body \n And it must be in farsi";
            case "title" -> "Based on the news body i gave you give me a seo based title \n And just return the answer nothing more\n And dont put the answer in anything just pure string \n And it must be in farsi ";
            case "lead" -> "Based on the news body i gave you give me a seo based lead \n And just return the answer nothing more\n And dont put the answer in anything just pure string \n And it must be in farsi ";
            case "headline" -> "Based on the news body i gave you give me a seo based headline \n And just return the answer nothing more\n And dont put the answer in anything just pure string  \n And it must be in farsi ";
            default -> throw new IllegalStateException("Invalid prompt type: " + promptType);
        };
        prompt = prompt + extraPrompt;
        String response = chatLanguageModel.chat(prompt);
        return response != null ? response : "No response from Gemini.";
    }

    public String translateToFarsi(String data) {
        String extra = "Translate this news to farsi\n if the text contains html tag dont touch the tags just translate the texts in it \n just return the answer nothing else\n";
        String prompt = extra + data;
        String response = chatLanguageModel.chat(prompt);

        return response != null ? response : "No response from Gemini.";
    }

    public String generateSlug(String persianKeyphrase, String title, String body) {
        StringBuilder slugPrompt = new StringBuilder();
        slugPrompt.append("Generate an SEO-friendly URL slug based on the following Persian content:\n\n");
        slugPrompt.append("Main Keyphrase (Focus): ").append(persianKeyphrase).append("\n");
        slugPrompt.append("Title: ").append(title).append("\n");
        slugPrompt.append("Content Preview: ").append(body.substring(0, Math.min(500, body.length()))).append("...\n\n");
        
        slugPrompt.append("Requirements:\n");
        slugPrompt.append("1- Translate the Persian keyphrase to English and use it as the main focus\n");
        slugPrompt.append("2- Create a concise, SEO-friendly slug (3-6 words maximum)\n");
        slugPrompt.append("3- Use lowercase letters only\n");
        slugPrompt.append("4- Use hyphens (-) to separate words, no spaces or underscores\n");
        slugPrompt.append("5- Remove all special characters, numbers, and punctuation\n");
        slugPrompt.append("6- Make it relevant to the news content and keyphrase\n");
        slugPrompt.append("7- Keep it under 60 characters total\n");
        slugPrompt.append("8- Return ONLY the slug, nothing else (no explanations or extra text)\n");
        slugPrompt.append("9- Example format: 'artificial-intelligence-news' or 'iran-foreign-policy'\n\n");
        
        String response = chatLanguageModel.chat(slugPrompt.toString());
        
        if (response != null) {
            // Clean up the response to ensure it's a proper slug
            response = response.trim()
                             .toLowerCase()
                             .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters
                             .replaceAll("\\s+", "-")         // Replace spaces with hyphens
                             .replaceAll("-+", "-")           // Replace multiple hyphens with single
                             .replaceAll("^-|-$", "");       // Remove leading/trailing hyphens
            
            // Ensure it's not too long
            if (response.length() > 60) {
                response = response.substring(0, 60).replaceAll("-[^-]*$", "");
            }
            
            return response.isEmpty() ? "generated-slug" : response;
        }
        
        return "generated-slug";
    }
}
