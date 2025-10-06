package com.avesta.mastercrawler.restcontroller;

import com.avesta.mastercrawler.service.IImagesService;
import com.avesta.mastercrawler.service.INewsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class GalleryRestController {

    private final IImagesService iImagesService;

    @PostMapping("/gallery/images/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("image") MultipartFile image) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "دسترسی غیرمجاز"));
        }
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "هیچ فایلی ارسال نشده است."));
        }
        iImagesService.imageUpload(image);
        return ResponseEntity.ok(Collections.singletonMap("message", "تصویر با موفقیت آپلود شد."));
    }


}
