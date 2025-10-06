package com.avesta.mastercrawler.controller;

import com.avesta.mastercrawler.model.Images;
import com.avesta.mastercrawler.model.News;
import com.avesta.mastercrawler.model.Videos;
import com.avesta.mastercrawler.service.IImagesService;
import com.avesta.mastercrawler.service.INewsService;
import com.avesta.mastercrawler.service.IVideosService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/gallery")
@AllArgsConstructor
public class GalleryController {

    private static final String GALLERY_PATH = "news/";
    private final IImagesService iImagesService;
    private final INewsService iNewsService;
    private final IVideosService iVideosService;

    @GetMapping("/list-images")
    public String imagesList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(required = false) String search,
            Model model) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Images> imagesPage = iImagesService.searchImage(search, pageRequest);


        model.addAttribute("images", imagesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", imagesPage.getTotalPages());
        model.addAttribute("totalItems", imagesPage.getTotalElements());
        model.addAttribute("searchQuery", search);
        return "gallery/gallery-list-images";
    }


    @GetMapping("/list-videos")
    public String videosList(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "16") int size,
                             @RequestParam(required = false) String search,
                             Model model) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Videos> videosPage = iVideosService.searchVideo(search, pageRequest);

        model.addAttribute("videos", videosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", videosPage.getTotalPages());
        model.addAttribute("totalItems", videosPage.getTotalElements());
        model.addAttribute("searchQuery", search);
        return "gallery/gallery-list-videos";
    }

    @PostMapping("/list-images-delete")
    public String imageDelete(@RequestParam("name") String name,RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)) {

            List<Images> foundImages = iImagesService.findAllByImageName(name);
            if (!foundImages.isEmpty()) {
                for(Images image: foundImages) {
                    List<News> foundNews = iNewsService.findByMainImage(image.getImageName());
                    if(!foundNews.isEmpty()) {
                        for(News item: foundNews) {
                            item.setMainImage(null);
                            iNewsService.save(item);
                        }
                    }
                    String directoryPath = "news/photos/";
                    String filePath = directoryPath + image.getImageName();
                    try {
                        Path fileToDelete = Paths.get(filePath);
                        if (Files.exists(fileToDelete)) {
                            Files.delete(fileToDelete);
                            System.out.println("File deleted successfully: " + filePath);
                        } else {
                            System.out.println("File not found: " + filePath);
                            redirectAttributes.addFlashAttribute("error", "فایل مورد نظر پیدا نشد: " + image.getImageName());
                        }
                    } catch (IOException e) {
                        System.out.println("Error deleting file: " + e.getMessage());
                        redirectAttributes.addFlashAttribute("error", "خطا در حذف فایل تصویر: " + image.getImageName());
                    }
                }
                foundImages.forEach(iImagesService::delete);
                redirectAttributes.addFlashAttribute("deleted", true);
            } else {
                redirectAttributes.addFlashAttribute("error", "تصویری با این نام یافت نشد.");
            }
        }
        return "redirect:/admin/gallery/list-images";
    }

    @PostMapping("/list-videos-delete")
    public String videoDelete(@RequestParam("name") String name,RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {

            List<Videos> foundVideo = iVideosService.findAllByVideoName(name);
            System.out.println(foundVideo);

            if(!foundVideo.isEmpty()) {
                for(Videos item: foundVideo) {
                    String directoryPath = "news/videos/";
                    String filePath = directoryPath + item.getVideoName();
                    try {
                        Path fileToDelete = Paths.get(filePath);
                        if (Files.exists(fileToDelete)) {
                            Files.delete(fileToDelete);
                            System.out.println("File deleted successfully: " + filePath);
                        } else {
                            System.out.println("File not found: " + filePath);
                            redirectAttributes.addFlashAttribute("error", "فایل مورد نظر پیدا نشد: " + item.getVideoName());
                        }
                    } catch (IOException e) {
                        System.out.println("Error deleting file: " + e.getMessage());
                        redirectAttributes.addFlashAttribute("error", "خطا در حذف فایل ویدیو: " + item.getVideoName());
                    }
                    List<News> newsVideoFound = iNewsService.findAllByVideoName(item.getVideoName());
                    for(News news: newsVideoFound) {
                        news.setMainVideo(null);
                        iNewsService.save(news);
                    }
                    iVideosService.delete(item);
                }
                foundVideo.forEach(iVideosService::delete);
                redirectAttributes.addFlashAttribute("deleted", true);
            }else {
                redirectAttributes.addFlashAttribute("error", "ویدیو با این نام یافت نشد.");
            }
        }
        return "redirect:/admin/gallery/list-videos";
    }

    @GetMapping("/gallery-browser")
    public String browseGallery(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "16") int size,
                                @RequestParam(required = false) String search,
                                Model model) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Images> imagesPage = iImagesService.searchImage(search, pageRequest);


        model.addAttribute("images", imagesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", imagesPage.getTotalPages());
        model.addAttribute("totalItems", imagesPage.getTotalElements());
        model.addAttribute("searchQuery", search);
        return "ckeditor-filemanager/filemanager";
    }

    @PostMapping("/list-images-add")
    public String addImage(@RequestParam("image") MultipartFile image, RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof  AnonymousAuthenticationToken)) {
            iImagesService.imageUpload(image);
            redirectAttributes.addFlashAttribute("success", true);
        }
        return "redirect:/admin/gallery/list-images";
    }

    @PostMapping("/list-images-link-add")
    public String addImageLink(@RequestParam("imageUrl") String imageUrl, RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            if (iImagesService.isValidImageUrl(imageUrl)) {
                iImagesService.downloadImage(imageUrl);
                redirectAttributes.addFlashAttribute("success", true);
            } else {
                redirectAttributes.addFlashAttribute("error", true);
            }
        }
        return "redirect:/admin/gallery/list-images";
    }

    @PostMapping("/list-images-edit")
    public String editImage(@RequestParam("imageId") Integer imageId,
                            @RequestParam("imageName") String newImageName,
                            RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Optional<Images> foundImage = iImagesService.findById(imageId);

            if (foundImage.isPresent()) {
                Images image = foundImage.get();
                String oldImageName = image.getImageName();
                String baseDirectory = "news/photos/";
                Path oldFilePath = Paths.get(baseDirectory + oldImageName);
                Path newFilePath = Paths.get(baseDirectory + newImageName);

                try {
                    Files.move(oldFilePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);

                    List<News> newsImages = iNewsService.findByMainImage(oldImageName);
                    for (News newsItem : newsImages) {
                        newsItem.setMainImage(newImageName);
                        iNewsService.save(newsItem);
                    }

                    image.setImageName(newImageName);
                    image.setImageSrc("/" + baseDirectory + newImageName);
                    iImagesService.save(image);

                    redirectAttributes.addFlashAttribute("success", true);
                } catch (IOException e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("error", "خطایی در تغییر نام فایل رخ داد.");
                }
            } else {
                redirectAttributes.addFlashAttribute("failed", true);
            }
        }

        return "redirect:/admin/gallery/list-images";
    }

    @PostMapping("/list-videos-add")
    public String addVideo(@RequestParam("video") MultipartFile video, RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof  AnonymousAuthenticationToken)) {
            iVideosService.videoUpload(video);
            redirectAttributes.addFlashAttribute("success", true);
        }
        return "redirect:/admin/gallery/list-videos";
    }

    @PostMapping("/list-videos-edit")
    public String editVideo(@RequestParam("videoId") Integer videoId,
                            @RequestParam("videoName") String newVideoName,
                            RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Optional<Videos> foundVideo = iVideosService.findById(videoId);

            if (foundVideo.isPresent()) {
                Videos video = foundVideo.get();
                String oldVideoName = video.getVideoName();
                String baseDirectory = "images/news/videos/";
                Path oldFilePath = Paths.get(baseDirectory + oldVideoName);
                Path newFilePath = Paths.get(baseDirectory + newVideoName);

                try {
                    Files.move(oldFilePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);

                    List<News> newsVideos = iNewsService.findByMainVideo(oldVideoName);
                    for (News newsItem : newsVideos) {
                        newsItem.setMainVideo(newVideoName);
                        iNewsService.save(newsItem);
                    }

                    video.setVideoName(newVideoName);
                    video.setVideoSrc("/" + baseDirectory + newVideoName);
                    iVideosService.save(video);

                    redirectAttributes.addFlashAttribute("success", true);
                } catch (IOException e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("error", "خطایی در تغییر نام فایل رخ داد.");
                }
            } else {
                redirectAttributes.addFlashAttribute("failed", true);
            }
        }

        return "redirect:/admin/gallery/list-videos";
    }

}
