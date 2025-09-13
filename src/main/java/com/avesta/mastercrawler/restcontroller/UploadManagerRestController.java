package com.avesta.mastercrawler.restcontroller;

import com.avesta.mastercrawler.model.Images;
import com.avesta.mastercrawler.model.Videos;
import com.avesta.mastercrawler.service.IImagesService;
import com.avesta.mastercrawler.service.IVideosService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UploadManagerRestController {

    private final IImagesService iImagesService;
    private final IVideosService iVideosService;

    @PostMapping("/image/upload")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile imageFile) {
        Map<String, String> response = new HashMap<>();

        if (imageFile.isEmpty()) {
            response.put("error", "هیچ فایلی ارسال نشده است.");
            return ResponseEntity.badRequest().body(response);
        }
        try {
            iImagesService.imageUpload(imageFile);
            response.put("success", "عکس شما با موفقیت ذخیره شد.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "خطایی در ذخیره‌سازی فایل رخ داد: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/image/fetch")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> fetchImage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Images> imagePage = iImagesService.findAll(pageable);

        List<Map<String, String>> images = new ArrayList<>();
        for (Images imageItem : imagePage.getContent()) {
            Map<String, String> imageMap = new HashMap<>();
            imageMap.put("imageUrl", imageItem.getImageSrc());
            images.add(imageMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("images", images);
        response.put("currentPage", imagePage.getNumber());
        response.put("totalPages", imagePage.getTotalPages());
        response.put("totalImages", imagePage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/video/fetch")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> fetchVideo() {
        List<Map<String, String>> response = new ArrayList<>();

        List<Videos> videos = iVideosService.findAll();

        for (Videos videoItem : videos) {
            Map<String, String> videoMap = new HashMap<>();
            videoMap.put("videoUrl", videoItem.getVideoSrc());
            response.add(videoMap);
        }

        System.out.println(response);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/video/upload")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadVideo(@RequestParam("video") MultipartFile videoFile) {
        Map<String, String> response = new HashMap<>();

        if (videoFile.isEmpty()) {
            response.put("error", "هیچ فایلی ارسال نشده است.");
            return ResponseEntity.badRequest().body(response);
        }
        try {
            iVideosService.videoUpload(videoFile);
            response.put("success", "ویدیوی شما با موفقیت ذخیره شد.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "خطایی در ذخیره‌سازی فایل رخ داد: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
