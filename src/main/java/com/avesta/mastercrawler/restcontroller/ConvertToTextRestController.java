package com.avesta.mastercrawler.restcontroller;

import com.avesta.mastercrawler.service.ai.WhisperService;
import com.avesta.mastercrawler.utility.FileUploadUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ConvertToTextRestController {
    private final WhisperService whisperService;
    private FileUploadUtil fileUploadUtil;

    @PostMapping("/upload/voice")
    public ResponseEntity<Map<String, Object>> uploadVoice(@RequestParam("voices") MultipartFile voices) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "دسترسی غیرمجاز"));
        }

        if (voices == null || voices.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "هیچ فایلی ارسال نشده است."));
        }

        try {
            String uploadDir = "news/voices";
            String filename = voices.getOriginalFilename();

            fileUploadUtil.saveFile(uploadDir, filename, voices);

            return ResponseEntity.ok(Collections.singletonMap("message", "فایل با موفقیت آپلود شد."));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "مشکل در آپلود فایل: " + e.getMessage()));
        }
    }

    @PostMapping("/getvoice")
    public ResponseEntity<Map<String, String>> convertToText(@RequestParam(required = false) String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            String fileSrc = "news/voices/" + fileName;
            String response = whisperService.convertToText(fileSrc);

            if (response != null) {
                return ResponseEntity.ok(Collections.singletonMap("transcript", response));
            } else {
                return ResponseEntity.status(500).body(Collections.singletonMap("error", "Could not convert the voice file to text."));
            }
        } else {
            return ResponseEntity.status(400).body(Collections.singletonMap("error", "File name is required."));
        }
    }

    @PostMapping("/upload/audio")
    public ResponseEntity<?> uploadAudio(@RequestParam("audio") MultipartFile audioFile,
                                         @RequestParam("filename") String filename) {
        try {
            String uploadDir = "news/voices";

            String fileExtension = getFileExtension(audioFile.getOriginalFilename());
            String fullFilename = filename + fileExtension;

            FileUploadUtil.saveFile(uploadDir, fullFilename, audioFile);

            return ResponseEntity.ok("فایل با موفقیت آپلود شد.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("مشکلی در آپلود فایل پیش آمده است.");
        }
    }


    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".mp4";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    @DeleteMapping("/delete/audio")
    public ResponseEntity<?> deleteAudio(@RequestParam("filename") String filename) {
        try {
            String uploadDir = "news/voices/";
            File fileToDelete = new File(uploadDir + filename);

            if (fileToDelete.exists()) {
                boolean deleted = fileToDelete.delete();
                if (deleted) {
                    return ResponseEntity.ok("فایل با موفقیت حذف شد.");
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("خطا در حذف فایل. لطفاً دوباره امتحان کنید.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("فایل یافت نشد.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("خطایی در حذف فایل رخ داده است.");
        }
    }




}
