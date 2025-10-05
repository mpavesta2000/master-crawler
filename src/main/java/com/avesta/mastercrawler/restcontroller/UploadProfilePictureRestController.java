package com.avesta.mastercrawler.restcontroller;

import com.avesta.mastercrawler.model.UserProfile;
import com.avesta.mastercrawler.service.IUserProfileService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UploadProfilePictureRestController {

    private final IUserProfileService iUserProfileService;


    @PostMapping("/profile/picture/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile imageFile) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserProfile> foundUser = iUserProfileService.findByEmail(authentication.getName());
        Map<String, String> response = new HashMap<>();

        if (imageFile.isEmpty()) {
            response.put("error", "هیچ فایلی ارسال نشده است.");
            return ResponseEntity.badRequest().body(response);
        }
        try {
            UserProfile profile = iUserProfileService.pictureUpload(imageFile, foundUser.get());
            iUserProfileService.save(profile);
            response.put("success", "عکس شما با موفقیت ذخیره شد.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "خطایی در ذخیره‌سازی فایل رخ داد: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


}
