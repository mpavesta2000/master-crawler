package com.avesta.mastercrawler.controller;

import com.avesta.mastercrawler.dto.NewsDto;
import com.avesta.mastercrawler.model.*;
import com.avesta.mastercrawler.service.*;
import com.avesta.mastercrawler.service.cms.AasaamCmsUploader;
import com.avesta.mastercrawler.utility.ImageDownloadUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/admin/news")
@AllArgsConstructor
public class NewsController {

    private final ICategoryService iCategoryService;
    private final INewsService iNewsService;
    private final IUsersService iUsersService;
    private final INewsTypeService iNewsTypeService;
    private final IImagesService iImagesService;
    private final IUserProfileService iUserProfileService;
    private final ITagsService iTagsService;
    private final ICommentsService iCommentsService;
    private final IUserMonthlyReportService iUserMonthlyReportService;
    private final AasaamCmsUploader aasaamCmsUploader;

    @GetMapping("/add")
    public String addForm(Model model) {
        List<Category> categoryList = iCategoryService.findAll();

        model.addAttribute("aasaamCategories", aasaamCmsUploader.getCategories());
        model.addAttribute("categories", categoryList);
        model.addAttribute("news", new News());

        return "news/news-add-new-ui";
    }

    @GetMapping("/add-video-news")
    public String addVideoForm(Model model) {
        List<Category> categoryList = iCategoryService.findAll();
        List<String> images = iNewsService.fileManagerImages();

        model.addAttribute("images", images);
        model.addAttribute("categories", categoryList);
        model.addAttribute("news", new News());

        return "news/news-add-video";
    }

    @PostMapping("/save-video")
    public String saveNewsVideo(RedirectAttributes redirectAttributes, @Valid News news, BindingResult bindingResult, Model model,
                                @RequestParam(required = false) String mainImage,
                                @RequestParam(required = false) String mainImageAltText,
                                @RequestParam(required = false) String mainVideo,
                                @RequestParam(required = false) boolean breakingNews,
                                @RequestParam(required = false) boolean newsHeadline,
                                @RequestParam(required = false) boolean showComments,
                                @RequestParam(required = false) boolean showMostViewed,
                                @RequestParam(required = false) boolean slider,
                                @RequestParam(required = false) boolean translate,
                                @RequestParam(required = false) boolean sendToTinn,
                                @RequestParam(required = false) List<String> newsTags,
                                @RequestParam(required = false) String date,
                                @RequestParam(required = false, name = "aasaamTinnCategories") List<String> aasaamTinnCategories,
                                @RequestParam(required = false, name = "aasaamTinnStatus") String aasaamTinnStatus,
                                @RequestParam(required = false, name = "yoastPoint") String yoastPoint) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {

            if (bindingResult.hasErrors()) {
                List<Category> categoryList = iCategoryService.findAll();
                model.addAttribute("categories", categoryList);
                model.addAttribute("news", news);
                model.addAttribute("errors", bindingResult.getAllErrors());
                return "news/news-add";
            }

            Users user = iUsersService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found."));
            NewsType newsType = iNewsTypeService.findById(2)
                    .orElseThrow(() -> new IllegalArgumentException("NewsType with id 2 not found"));

            if (news.getId() != null) {
                Optional<News> existingNews = iNewsService.findById(news.getId());
                if (existingNews.isPresent()) {
                    news.setChapChin(existingNews.get().getChapChin());
                    news.setComments(existingNews.get().getComments());
                    news.setUserId(existingNews.get().getUserId());

                    if (yoastPoint == null || yoastPoint.trim().isEmpty()) {
                        news.setYoastSeoPoint(existingNews.get().getYoastSeoPoint());
                    }

                    if (news.getCreatedAt() == null) {
                        news.setCreatedAt(existingNews.get().getCreatedAt());
                    }
                }
            } else {
                news.setUserId(user);
            }

            //Add tags for a new news
            if(news.getId() == null) {
                if(newsTags != null) {
                    List<Tags> newTags = new ArrayList<>();
                    for(String item : newsTags) {
                        Tags addTags = iTagsService.addTagIfNotExists(item);
                        newTags.add(addTags);
                    }
                    news.setTags(newTags);
                }else {
                    news.setTags(new ArrayList<>());
                }
            }

            //Add tags for an existing news
            if(news.getId() != null) {
                Optional<News> foundedNews = iNewsService.findById(news.getId());
                if(foundedNews.isPresent()) {
                    if(newsTags == null) {
                        if(!foundedNews.get().getTags().isEmpty()) {
                            news.setTags(new ArrayList<>());
                        }
                    } else {
                        List<Tags> newTags = new ArrayList<>();
                        for(String item : newsTags) {
                            Tags addTags = iTagsService.addTagIfNotExists(item);
                            newTags.add(addTags);
                        }
                        List<Tags> commonTags = new ArrayList<>(foundedNews.get().getTags());
                        commonTags.retainAll(newTags);
                        Set<Tags> mergedTags = new HashSet<>(commonTags);
                        for (Tags tag : newTags) {
                            Tags modifiedTags = iTagsService.addTagIfNotExists(tag.getName());
                            mergedTags.add(modifiedTags);
                        }
                        List<Tags> finalTags = new ArrayList<>(mergedTags);
                        news.setTags(finalTags);
                    }
                }
            }


            news.setBreakingNews(breakingNews);
            news.setNewsHeadline(newsHeadline);
            news.setShowComments(showComments);
            news.setShowMostViewed(showMostViewed);
            news.setSlider(slider);
            news.setGetTranslated(translate);
            news.setNewsTypeId(newsType);

            //Upload images in news body
            String updatedBody = ImageDownloadUtil.processImages(news.getBody(),iImagesService);
            news.setBody(updatedBody);

            //Upload main image for news
            if (mainImage != null) {
                news.setMainImage(mainImage);
            }

            //Set main image alt text
            if (mainImageAltText != null && !mainImageAltText.trim().isEmpty()) {
                news.setMainImageAltText(mainImageAltText);
            }

            //Check new news before saving
            boolean isNew = !iNewsService.findByTitle(news.getTitle()).isPresent();

            // For existing news, get the original createdAt
            if (!isNew) {
                Optional<News> existingNews = iNewsService.findByTitle(news.getTitle());
                if (existingNews.isPresent() && news.getCreatedAt() == null) {
                    news.setCreatedAt(existingNews.get().getCreatedAt());
                }
            }

            if(yoastPoint != null && !yoastPoint.trim().isEmpty()) {
                news.setYoastSeoPoint(Long.parseLong(yoastPoint));
            }

            // Save the news
            News savedNews = iNewsService.save(news);

            if (isNew) {
                Long seoPoint = savedNews.getYoastSeoPoint();
                iUserMonthlyReportService.updateUserReport(savedNews, seoPoint != null ? seoPoint : 0L);
                System.out.println("=== NEW NEWS SAVED ===");
                System.out.println("Title: " + savedNews.getTitle());
                System.out.println("Created: " + savedNews.getCreatedAt());
                System.out.println("Updated: " + savedNews.getUpdatedAt());
            } else {
                System.out.println("=== EXISTING NEWS EDITED ===");
                System.out.println("Title: " + savedNews.getTitle());
                System.out.println("Created: " + savedNews.getCreatedAt());
                System.out.println("Updated: " + savedNews.getUpdatedAt());
                System.out.println("Is Updated timestamp different: " + (savedNews.getUpdatedAt() != null && !savedNews.getUpdatedAt().equals(savedNews.getCreatedAt())));
                iUserMonthlyReportService.updateUserReportForEdit(savedNews);
                System.out.println("Edit tracking updated for user: " + savedNews.getUserId().getEmail());
            }

            // If video is provided, upload video
            if (mainVideo != null) {
                news.setMainVideo(mainVideo);
                savedNews = iNewsService.save(news);
            }

            // Edit date if its not null
            if(date != null && !date.trim().isEmpty()) {
                LocalDateTime parsedDate = LocalDateTime.parse(date,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                iNewsService.updateCreatedAtById(savedNews.getId(), parsedDate);
            }

            //FOR TINN NEWS
            // if(sendToTinn) {
            //     String response = aasaamCmsUploader.uploadNews(savedNews, aasaamTinnCategories, "",aasaamTinnStatus);
            //     savedNews.setAasaamNewsId(Integer.parseInt(response));
            //     savedNews = iNewsService.save(savedNews);
            //     System.out.println(response);
            // }

            // After successful video news save, awake SweetAlert
            if (savedNews.getId() != null) {
                redirectAttributes.addFlashAttribute("success", true);
            }
        }
        return "redirect:/admin/news/add-video-news";
    }

    @GetMapping("/video/edit/{id}")
    public String editNewsVideo(Model model, @PathVariable("id") Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)) {
            Optional<News> foundNews = iNewsService.findById(id);
            List<Category> categoryList = iCategoryService.findAll();

            model.addAttribute("categories", categoryList);
            model.addAttribute("news", foundNews.get());
        }
        return "news/news-add-video";
    }

    @GetMapping("/add-image-news")
    public String addImageForm(Model model) {
        List<Category> categoryList = iCategoryService.findAll();
        List<String> images = iNewsService.fileManagerImages();

        model.addAttribute("images", images);
        model.addAttribute("categories", categoryList);
        model.addAttribute("news", new News());

        return "news/news-add-image";
    }

    @PostMapping("/save-images-news")
    public String saveNewsImage(RedirectAttributes redirectAttributes,Model model, @RequestParam("images") List<String> images, @RequestParam(required = false) String mainImage, @RequestParam(required = false) String mainImageAltText, @Valid News news, BindingResult bindingResult,
                                @RequestParam(required = false) String mainVideo,
                                @RequestParam(required = false) boolean breakingNews,
                                @RequestParam(required = false) boolean newsHeadline,
                                @RequestParam(required = false) boolean showComments,
                                @RequestParam(required = false) boolean showMostViewed,
                                @RequestParam(required = false) boolean slider,
                                @RequestParam(required = false) boolean translate,
                                @RequestParam(required = false) boolean sendToTinn,
                                @RequestParam(required = false) List<String> newsTags,
                                @RequestParam(required = false) String date,
                                @RequestParam(required = false, name = "aasaamTinnCategories") List<String> aasaamTinnCategories,
                                @RequestParam(required = false, name = "aasaamTinnStatus") String aasaamTinnStatus,
                                @RequestParam(required = false, name = "yoastPoint") String yoastPoint) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)) {

            if (bindingResult.hasErrors()) {
                List<Category> categoryList = iCategoryService.findAll();
                model.addAttribute("categories", categoryList);
                model.addAttribute("news", news);
                model.addAttribute("errors", bindingResult.getAllErrors());
                return "news/news-add";
            }

            Users user = iUsersService.findByEmail(authentication.getName()).
                    orElseThrow(()->new UsernameNotFoundException("user not found."));
            NewsType newsType = iNewsTypeService.findById(1)
                    .orElseThrow(() -> new IllegalArgumentException("NewsType with id 1 not found"));

            if (news.getId() != null) {
                Optional<News> existingNews = iNewsService.findById(news.getId());
                if (existingNews.isPresent()) {
                    news.setChapChin(existingNews.get().getChapChin());
                    news.setComments(existingNews.get().getComments());
                    news.setUserId(existingNews.get().getUserId());

                    if (yoastPoint == null || yoastPoint.trim().isEmpty()) {
                        news.setYoastSeoPoint(existingNews.get().getYoastSeoPoint());
                    }

                    if (news.getCreatedAt() == null) {
                        news.setCreatedAt(existingNews.get().getCreatedAt());
                    }
                }
            } else {
                news.setUserId(user);
            }

            //Add tags for a new news
            if(news.getId() == null) {
                if(newsTags != null) {
                    List<Tags> newTags = new ArrayList<>();
                    for(String item : newsTags) {
                        Tags addTags = iTagsService.addTagIfNotExists(item);
                        newTags.add(addTags);
                    }
                    news.setTags(newTags);
                }else {
                    news.setTags(new ArrayList<>());
                }
            }

            //Add tags for an existing news
            if(news.getId() != null) {
                Optional<News> foundedNews = iNewsService.findById(news.getId());
                if(foundedNews.isPresent()) {
                    if(newsTags == null) {
                        if(!foundedNews.get().getTags().isEmpty()) {
                            news.setTags(new ArrayList<>());
                        }
                    } else {
                        List<Tags> newTags = new ArrayList<>();
                        for(String item : newsTags) {
                            Tags addTags = iTagsService.addTagIfNotExists(item);
                            newTags.add(addTags);
                        }
                        List<Tags> commonTags = new ArrayList<>(foundedNews.get().getTags());
                        commonTags.retainAll(newTags);
                        Set<Tags> mergedTags = new HashSet<>(commonTags);
                        for (Tags tag : newTags) {
                            Tags modifiedTags = iTagsService.addTagIfNotExists(tag.getName());
                            mergedTags.add(modifiedTags);
                        }
                        List<Tags> finalTags = new ArrayList<>(mergedTags);
                        news.setTags(finalTags);
                    }
                }
            }

            news.setBreakingNews(breakingNews);
            news.setNewsHeadline(newsHeadline);
            news.setShowComments(showComments);
            news.setShowMostViewed(showMostViewed);
            news.setSlider(slider);
            news.setGetTranslated(translate);
            news.setNewsTypeId(newsType);


            //Upload images in news body
            String updatedBody = ImageDownloadUtil.processImages(news.getBody(), iImagesService);
            news.setBody(updatedBody);

            // Check if news already exists (editing scenario)
            List<String> existingImageList = new ArrayList<>();
            if (news.getId() != null) {
                Optional<News> existingNewsOpt = iNewsService.findById(news.getId());
                if (existingNewsOpt.isPresent()) {
                    News existingNews = existingNewsOpt.get();
                    existingImageList = existingNews.getImageList();
                }
            }

            // Upload and merge new images with existing ones
            existingImageList.addAll(images);
            news.setImageList(existingImageList);

            //Upload main image for news
            if (mainImage != null) {
                news.setMainImage(mainImage);
            }

            //Set main image alt text
            if (mainImageAltText != null && !mainImageAltText.trim().isEmpty()) {
                news.setMainImageAltText(mainImageAltText);
            }

            //Check new news before saving
            boolean isNew = !iNewsService.findByTitle(news.getTitle()).isPresent();

            // For existing news, get the original createdAt
            if (!isNew) {
                Optional<News> existingNews = iNewsService.findByTitle(news.getTitle());
                if (existingNews.isPresent() && news.getCreatedAt() == null) {
                    news.setCreatedAt(existingNews.get().getCreatedAt());
                }
            }

            if(yoastPoint != null && !yoastPoint.trim().isEmpty()) {
                news.setYoastSeoPoint(Long.parseLong(yoastPoint));
            }

            // Save the news
            News savedNews = iNewsService.save(news);

            if (isNew) {
                Long seoPoint = savedNews.getYoastSeoPoint();
                iUserMonthlyReportService.updateUserReport(savedNews, seoPoint != null ? seoPoint : 0L);
                System.out.println("=== NEW NEWS SAVED ===");
                System.out.println("Title: " + savedNews.getTitle());
                System.out.println("Created: " + savedNews.getCreatedAt());
                System.out.println("Updated: " + savedNews.getUpdatedAt());
            } else {
                System.out.println("=== EXISTING NEWS EDITED ===");
                System.out.println("Title: " + savedNews.getTitle());
                System.out.println("Created: " + savedNews.getCreatedAt());
                System.out.println("Updated: " + savedNews.getUpdatedAt());
                System.out.println("Is Updated timestamp different: " + (savedNews.getUpdatedAt() != null && !savedNews.getUpdatedAt().equals(savedNews.getCreatedAt())));
                iUserMonthlyReportService.updateUserReportForEdit(savedNews);
                System.out.println("Edit tracking updated for user: " + savedNews.getUserId().getEmail());
            }

            // If video is provided, upload video
            if (mainVideo != null) {
                news.setMainVideo(mainVideo);
                savedNews = iNewsService.save(news);
            }

            // Edit date if its not null
            if(date != null && !date.trim().isEmpty()) {
                LocalDateTime parsedDate = LocalDateTime.parse(date,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                iNewsService.updateCreatedAtById(savedNews.getId(), parsedDate);
            }

            //FOR TINN NEWS
            // if(sendToTinn) {
            //     String response = aasaamCmsUploader.uploadNews(savedNews, aasaamTinnCategories, "",aasaamTinnStatus);
            //     savedNews.setAasaamNewsId(Integer.parseInt(response));
            //     savedNews = iNewsService.save(savedNews);
            //     System.out.println(response);
            // }


            //After successful Gallery news save awake sweetalert
            if(savedNews.getId() != null) {
                redirectAttributes.addFlashAttribute("success", true);
            }
        }
        return "redirect:/admin/news/add-image-news";
    }

    @GetMapping("/images-news/edit/{id}")
    public String editNewsImage(Model model, @PathVariable("id") Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)) {
            Optional<News> foundNews = iNewsService.findById(id);
            List<Category> categoryList = iCategoryService.findAll();
            List<String> images = iNewsService.fileManagerImages();

            model.addAttribute("images", images);
            model.addAttribute("categories", categoryList);
            model.addAttribute("news", foundNews.get());
        }
        return "news/news-add-image";
    }

    @PostMapping("/save")
    public String saveNews(RedirectAttributes redirectAttributes, @Valid News news, BindingResult bindingResult, Model model,
                           @RequestParam(required = false) String mainImage,
                           @RequestParam(required = false) String mainImageAltText,
                           @RequestParam(required = false) String mainVideo,
                           @RequestParam(required = false) boolean breakingNews,
                           @RequestParam(required = false) boolean newsHeadline,
                           @RequestParam(required = false) boolean showComments,
                           @RequestParam(required = false) boolean showMostViewed,
                           @RequestParam(required = false) boolean slider,
                           @RequestParam(required = false) boolean translate,
                           @RequestParam(required = false) boolean sendToTinn,
                           @RequestParam(required = false) List<String> newsTags,
                           @RequestParam(required = false) String date,
                           @RequestParam(required = false, name = "aasaamTinnCategories") List<String> aasaamTinnCategories,
                           @RequestParam(required = false, name = "aasaamTinnStatus") String aasaamTinnStatus,
                           @RequestParam(required = false, name = "yoastPoint") String yoastPoint) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)) {

            if (bindingResult.hasErrors()) {
                List<Category> categoryList = iCategoryService.findAll();
                model.addAttribute("categories", categoryList);
                model.addAttribute("news", news);
                model.addAttribute("errors", bindingResult.getAllErrors());
                return "news/news-add";
            }

            System.out.println(yoastPoint);

            Users user = iUsersService.findByEmail(authentication.getName()).orElseThrow(()->new UsernameNotFoundException("user not found."));
            NewsType newsType = iNewsTypeService.findById(3)
                    .orElseThrow(() -> new IllegalArgumentException("NewsType with id 3 not found"));

            if (news.getId() != null) {
                Optional<News> existingNews = iNewsService.findById(news.getId());
                if (existingNews.isPresent()) {
                    news.setChapChin(existingNews.get().getChapChin());
                    news.setComments(existingNews.get().getComments());
                    news.setUserId(existingNews.get().getUserId());

                    if (yoastPoint == null || yoastPoint.trim().isEmpty()) {
                        news.setYoastSeoPoint(existingNews.get().getYoastSeoPoint());
                    }

                    if (news.getCreatedAt() == null) {
                        news.setCreatedAt(existingNews.get().getCreatedAt());
                    }
                }
            } else {
                news.setUserId(user);
            }

            //Add tags for a new news
            if(news.getId() == null) {
                if(newsTags != null) {
                    List<Tags> newTags = new ArrayList<>();
                    for(String item : newsTags) {
                        Tags addTags = iTagsService.addTagIfNotExists(item);
                        newTags.add(addTags);
                    }
                    news.setTags(newTags);
                }else {
                    news.setTags(new ArrayList<>());
                }
            }

            //Add tags for an existing news
            if(news.getId() != null) {
                Optional<News> foundedNews = iNewsService.findById(news.getId());
                if(foundedNews.isPresent()) {
                    if(newsTags == null) {
                        if(!foundedNews.get().getTags().isEmpty()) {
                            news.setTags(new ArrayList<>());
                        }
                    } else {
                        List<Tags> newTags = new ArrayList<>();
                        for(String item : newsTags) {
                            Tags addTags = iTagsService.addTagIfNotExists(item);
                            newTags.add(addTags);
                        }
                        List<Tags> commonTags = new ArrayList<>(foundedNews.get().getTags());
                        commonTags.retainAll(newTags);
                        Set<Tags> mergedTags = new HashSet<>(commonTags);
                        for (Tags tag : newTags) {
                            Tags modifiedTags = iTagsService.addTagIfNotExists(tag.getName());
                            mergedTags.add(modifiedTags);
                        }
                        List<Tags> finalTags = new ArrayList<>(mergedTags);
                        news.setTags(finalTags);
                    }
                }
            }

            news.setBreakingNews(breakingNews);
            news.setNewsHeadline(newsHeadline);
            news.setShowComments(showComments);
            news.setSlider(slider);
            news.setShowMostViewed(showMostViewed);
            news.setGetTranslated(translate);
            news.setNewsTypeId(newsType);

            //Upload images in news body
            String updatedBody = ImageDownloadUtil.processImages(news.getBody(), iImagesService);
            news.setBody(updatedBody);

            //Upload main image for news
            if (mainImage != null) {
                news.setMainImage(mainImage);
            }

            //Set main image alt text
            if (mainImageAltText != null && !mainImageAltText.trim().isEmpty()) {
                news.setMainImageAltText(mainImageAltText);
            }

            //Check new news before saving
            boolean isNew = !iNewsService.findByTitle(news.getTitle()).isPresent();

            // For existing news, get the original createdAt
            if (!isNew) {
                Optional<News> existingNews = iNewsService.findByTitle(news.getTitle());
                if (existingNews.isPresent() && news.getCreatedAt() == null) {
                    news.setCreatedAt(existingNews.get().getCreatedAt());
                }
            }

            if(yoastPoint != null && !yoastPoint.trim().isEmpty()) {
                news.setYoastSeoPoint(Long.parseLong(yoastPoint));
            }

            // Save the news
            News savedNews = iNewsService.save(news);

            if (isNew) {
                Long seoPoint = savedNews.getYoastSeoPoint();
                iUserMonthlyReportService.updateUserReport(savedNews, seoPoint != null ? seoPoint : 0L);
                System.out.println("=== NEW NEWS SAVED ===");
                System.out.println("Title: " + savedNews.getTitle());
                System.out.println("Created: " + savedNews.getCreatedAt());
                System.out.println("Updated: " + savedNews.getUpdatedAt());
            } else {
                System.out.println("=== EXISTING NEWS EDITED ===");
                System.out.println("Title: " + savedNews.getTitle());
                System.out.println("Created: " + savedNews.getCreatedAt());
                System.out.println("Updated: " + savedNews.getUpdatedAt());
                System.out.println("Is Updated timestamp different: " + (savedNews.getUpdatedAt() != null && !savedNews.getUpdatedAt().equals(savedNews.getCreatedAt())));
                iUserMonthlyReportService.updateUserReportForEdit(savedNews);
                System.out.println("Edit tracking updated for user: " + savedNews.getUserId().getEmail());
            }

            // If video is provided, upload video
            if (mainVideo != null) {
                news.setMainVideo(mainVideo);
                savedNews = iNewsService.save(news);
            }

            // Edit date if its not null
            if(date != null && !date.trim().isEmpty()) {
                LocalDateTime parsedDate = LocalDateTime.parse(date,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                iNewsService.updateCreatedAtById(savedNews.getId(), parsedDate);
            }


            //FOR TINN NEWS
            // if(sendToTinn) {
            //     String response = aasaamCmsUploader.uploadNews(savedNews, aasaamTinnCategories, "",aasaamTinnStatus);
            //     savedNews.setAasaamNewsId(Integer.parseInt(response));
            //     savedNews = iNewsService.save(savedNews);
            //     System.out.println(response);
            // }

            // After successful save, trigger SweetAlert
            if(savedNews.getId() != null) {
                redirectAttributes.addFlashAttribute("success", true);
            }
        }
        return "redirect:/admin/news/add";
    }

    @PostMapping("/search")
    public String searchNews(RedirectAttributes redirectAttributes,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size,
                             @RequestParam(value = "search", required = false) String search,
                             @RequestParam(value = "status", required = false) String status) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {

            List<News> news = iNewsService.searchNews(search, status);
            List<NewsDto> newsDtos = new ArrayList<>();
            for (int i = 0; i < news.size(); i++) {
                NewsDto dto = new NewsDto();
                dto.setNewsId(news.get(i).getId());
                dto.setNewsHeadLine(news.get(i).getTitle());
                dto.setNewsLead(news.get(i).getLead());
                dto.setNewsBody(news.get(i).getBody());
                dto.setUserId(news.get(i).getUserId());
                dto.setCategories(news.get(i).getCategories());
                dto.setStatus(news.get(i).getStatus());
                dto.setNewsType(news.get(i).getNewsTypeId().getNewsTypeName());

                newsDtos.add(dto);
            }


            if(newsDtos.isEmpty() || newsDtos == null) {
                redirectAttributes.addFlashAttribute("notFound", true);
            }

            redirectAttributes.addFlashAttribute("newsDtos", newsDtos);
            redirectAttributes.addFlashAttribute("search", search);
            redirectAttributes.addFlashAttribute("status", status);
        }
        return "redirect:/admin/news/list";
    }

    @GetMapping("/list")
    public String showList(Model model) {
        model.addAttribute("userNames", iUsersService.findAll());
        return "news/news-list";
    }

    @GetMapping("/edit/{id}")
    public String editNews(Model model, @PathVariable("id") Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)) {
            Optional<News> foundNews = iNewsService.findById(id);
            List<Category> categoryList = iCategoryService.findAll();
            List<String> images = iNewsService.fileManagerImages();
            System.out.println(aasaamCmsUploader.getCategories());

            model.addAttribute("aasaamCategories", aasaamCmsUploader.getCategories());
            model.addAttribute("images", images);
            model.addAttribute("categories", categoryList);
            model.addAttribute("news", foundNews.get());
        }
        return "news/news-add-new-ui";
    }

    @PostMapping("/delete/{id}")
    public String deleteNews(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            News news = iNewsService.findById(id).orElseThrow(() -> new NullPointerException("news not found"));
            YearMonth reportMonth = YearMonth.from(news.getCreatedAt());
            iUserMonthlyReportService.findByUserAndReportMonth(news.getUserId(), reportMonth)
                    .ifPresent(report -> {
                        if (news.getChapChin() == true) {
                            report.setChapChinCounter(report.getChapChinCounter() - 1);
                        } else {
                            report.setNewsCounter(report.getNewsCounter() - 1);
                        }
                        double averageSeoPoints = iNewsService.findByUserId(news.getUserId())
                                .stream()
                                .filter(n -> !n.getId().equals(id))
                                .mapToLong(News::getYoastSeoPoint)
                                .average()
                                .orElse(0);
                        report.setSeoPointAvg((long) averageSeoPoints);
                        iUserMonthlyReportService.save(report);
                    });

            iNewsService.deleteById(id);
            redirectAttributes.addFlashAttribute("deleted", true);
        }
        return "redirect:/admin/news/list";
    }

    @GetMapping("/sort")
    public String newsSort(Model model) {
        return "news/news-sort";
    }

    @PostMapping("/sort/{section}")
    public String newsSection(RedirectAttributes redirectAttributes,@PathVariable("section") String section) {
        return "redirect:/admin/news/sort";
    }

    @GetMapping("/show/{id}")
    public String showNews(@PathVariable("id") Integer id, Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)) {
            Optional<Users> userProfile = iUsersService.findByEmail(authentication.getName());
            Optional<News> foundNews = iNewsService.findById(id);
            if(foundNews.isPresent()) {
                model.addAttribute("comment", new Comments());
                model.addAttribute("userProfile", userProfile.get().getUserProfile());
                model.addAttribute("news", foundNews.get());
            } else {
                System.out.println("not found");
            }
        }
        return "news/news-overview";
    }

}
