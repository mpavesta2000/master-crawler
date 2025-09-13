package com.avesta.mastercrawler.restcontroller;

import com.avesta.mastercrawler.model.*;
import com.avesta.mastercrawler.service.*;
import com.avesta.mastercrawler.service.scraper.ChapChinService;
import com.avesta.mastercrawler.utility.ImageDownloadUtil;
import kong.unirest.JsonNode;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ChapChinRestController {

    private final ChapChinService chapChinService;
    private final ICategoryService iCategoryService;
    private final IUsersService iUsersService;
    private final IUsersTypeService iUsersTypeService;
    private final INewsService iNewsService;
    private final INewsTypeService iNewsTypeService;
    private final IImagesService iImagesService;

    @PostMapping("/chapchin/send")
    public ResponseEntity<Map<String, String>> chapchinSend(@RequestBody Map<String, String> requestBody) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            try {
                Users user = iUsersService.findByEmail(authentication.getName())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found."));
                NewsType newsType = iNewsTypeService.findById(3)
                        .orElseThrow(() -> new IllegalArgumentException("NewsType with id 3 not found"));

                String url = requestBody.get("url");
                JsonNode response = chapChinService.chapChinResponse(url);
                System.out.println(response);

                String statusCode = response.getObject().has("status") ? response.getObject().getString("status") : "1";

                if (response == null || Integer.parseInt(statusCode) == 403) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("message", "سایت موردنظر پشتیبانی نمی‌شود.");
                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }


                Category defaultCategory = iCategoryService.defaultCategory();
                List<Category> updatedCategories = new ArrayList<>();
                updatedCategories.add(defaultCategory);

                News news = new News();
                news.setNewsTypeId(newsType);
                news.setUserId(user);
                if(response.getObject().get("headline") != null) {
                    news.setHeadline(response.getObject().getString("headline"));
                }
                news.setTitle(response.getObject().getString("title"));
                news.setLead(response.getObject().getString("lead"));
                news.setStatus("Draft");
                news.setCategories(updatedCategories);

                String updatedBody = ImageDownloadUtil.processImages(response.getObject().getString("content"), iImagesService);
                news.setBody(updatedBody);

                try {
                    String uploadDir = "news/photos/";
                    Files.createDirectories(Paths.get(uploadDir));
                    String imageName = "news_image_" + System.currentTimeMillis() + ".jpg";
                    Path imagePath = Paths.get(uploadDir, imageName);
                    news.setMainImage(imageName);
                    try (InputStream in = new URL(response.getObject().getString("cover")).openStream()) {
                        Files.copy(in, imagePath);
                        Images gallery = new Images(imageName, '/' + uploadDir + imageName);
                        iImagesService.save(gallery);
                    }
                } catch (IOException e) {
                    throw new IOException("Error downloading or saving image: " + response.getObject().getString("cover"), e);
                }

                iNewsService.save(news);

                Map<String, String> responseSuccess = new HashMap<>();
                responseSuccess.put("message", "اطلاعات با موفقیت ذخیره شد!");
                return new ResponseEntity<>(responseSuccess, HttpStatus.OK);

            } catch (Exception e) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "خطا در پردازش درخواست: " + e.getMessage());
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "اطلاعات ذخیره نشده اند!");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

}
