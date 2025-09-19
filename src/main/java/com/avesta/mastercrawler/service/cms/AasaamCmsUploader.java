package com.avesta.mastercrawler.service.cms;

import com.avesta.mastercrawler.model.News;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mfathi91.time.PersianDate;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AasaamCmsUploader {

    private static final String API_KEY = "G4rivDevEdockiFrabHoiss1cddiponeOjNojCuimm0j7OcVuwoHuft8Quoc";

    public Map<String, String> getCategories() {
        try {
            HttpResponse<String> response = Unirest.get("https://tinn.ir/newsstudioapis/getcategorylist?APIKEY="+API_KEY)
                    .asString();

            ObjectMapper mapper = new ObjectMapper();

            List<Map<String, Object>> list = mapper.readValue(response.getBody(), new TypeReference<List<Map<String, Object>>>() {});

            return list.stream()
                    .filter(item -> item.get("id") != null && item.get("title") != null)
                    .collect(Collectors.toMap(
                            item -> item.get("id").toString(),
                            item -> item.get("title").toString()
                    ));

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    public String uploadFileAndGetId(String url) {
        String body = "{\"urls\":\"" + url + "\"}";
        HttpResponse<JsonNode> response = Unirest.post("https://www.tinn.ir/newsstudioapis/fileuploaderurl?APIKEY=" + API_KEY)
                .header("accept", "application/json, text/plain, */*")
                .header("content-type", "application/json;charset=UTF-8")
                .body(body)
                .asJson();
        JSONArray ids = response.getBody().getArray();
        if (ids.length() > 0) {
            return ids.getString(0);
        }
        return null;
    }

    public String getFileUrl(String id) {
        String body = "{\"ids\":\"" + id + "\"}";
        try {
            HttpResponse<String> response = Unirest.post("https://www.tinn.ir/newsstudioapis/filesdata?APIKEY=" + API_KEY)
                    .header("accept", "application/json, text/plain, */*")
                    .header("content-type", "application/json;charset=UTF-8")
                    .body(body)
                    .asString();

            ObjectMapper mapper = new ObjectMapper();

            Map<String, Map<String, Object>> map = mapper.readValue(
                    response.getBody(),
                    new TypeReference<Map<String, Map<String, Object>>>() {}
            );

            Map<String, Object> fileData = map.get(String.valueOf(id));
            if (fileData != null) {
                return fileData.get("url").toString();
            }else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private String uploadAndGetNewUrl(String originalUrl) {
        try {
            String fileId = uploadFileAndGetId(originalUrl);
            if (fileId == null) {
                System.err.println("Failed to get file ID for: " + originalUrl);
                return null;
            }

            String newUrl = getFileUrl(fileId);
            if (newUrl == null) {
                System.err.println("Failed to get file URL for ID: " + fileId);
                return null;
            }

            return newUrl;

        } catch (Exception e) {
            System.err.println("Error processing image " + originalUrl + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String processAllNewsBodyImages(String newsBody) {
        if (newsBody == null || newsBody.trim().isEmpty()) {
            return newsBody;
        }

        Document doc = Jsoup.parse(newsBody);

        Elements imgTags = doc.select("img");

        Map<String, String> urlReplacementCache = new HashMap<>();

        for (Element imgTag : imgTags) {
            String originalUrl = imgTag.attr("src");

            if (originalUrl == null || originalUrl.trim().isEmpty()) {
                continue;
            }

            if (originalUrl.startsWith("data:") || originalUrl.contains("tinn.ir")) {
                continue;
            }

            String newUrl = urlReplacementCache.get(originalUrl);

            if (newUrl == null) {
                newUrl = uploadAndGetNewUrl(originalUrl);
                if (newUrl != null) {
                    urlReplacementCache.put(originalUrl, newUrl);
                }
            }

            if (newUrl != null) {
                imgTag.attr("src", newUrl);
                System.out.println("Replaced: " + originalUrl + " -> " + newUrl);
            } else {
                System.err.println("Failed to upload image: " + originalUrl);
            }
        }
        return doc.html();
    }

    public String formatDate(String time) {
        try {
            LocalDateTime dateTime;
            if (time.contains("T")) {
                if (time.length() == 16) {
                    time = time + ":00";
                }
                dateTime = LocalDateTime.parse(time);
            } else {
                dateTime = LocalDateTime.parse(time,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
            }

            PersianDate persianDate = PersianDate.fromGregorian(dateTime.toLocalDate());

            String persianDateStr = String.format("%04d/%02d/%02d %02d:%02d:%02d",
                    persianDate.getYear(),
                    persianDate.getMonthValue(),
                    persianDate.getDayOfMonth(),
                    dateTime.getHour(),
                    dateTime.getMinute(),
                    dateTime.getSecond());

            return persianDateStr;

        } catch (Exception e) {
            System.err.println("Error parsing date '" + time + "': " + e.getMessage());
            e.printStackTrace();
            return time;
        }
    }

    public String uploadNews(News news, List<String> category,String serverDomain) {
        String type = "2";
        String publish = "0";
        String productionType = "1";
        String primaryFileId = uploadFileAndGetId(serverDomain+news.getMainImage());
        String content = processAllNewsBodyImages(news.getBody());
        String publishTime = formatDate(news.getCreatedAt().toString());
        String slug = news.getSlug();
        String tags = news.getTags().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.joining(","));
        String categories = category.stream()
                .collect(Collectors.joining(","));
        

        return null;
    }

}
