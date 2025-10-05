package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.Images;
import com.avesta.mastercrawler.model.News;
import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.repository.ImagesRepository;
import com.avesta.mastercrawler.repository.NewsRepository;
import com.avesta.mastercrawler.service.INewsService;
import com.avesta.mastercrawler.utility.FileUploadUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class NewsServiceImpl implements INewsService {

    private final NewsRepository newsRepository;
    private final ImagesRepository imagesRepository;

    @Override
    public News save(News news) {
        return newsRepository.save(news);
    }

    @Override
    public Page<News> findAll(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    @Override
    public Optional<News> findByTitle(String title) {
        return newsRepository.findByTitle(title);
    }

    @Override
    public Optional<News> findById(Integer id) {
        return newsRepository.findById(id);
    }

    @Override
    public List<String> fileManagerImages() {
        File directory = new File("news/photos/");
        File[] files = directory.listFiles((dir, name) -> name.matches(".*\\.(jpg|jpeg|png|gif)$"));
        List<String> imageUrls = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                imageUrls.add("news/photos/" + file.getName());
            }
        }
        return imageUrls;
    }

    @Override
    public void deleteById(Integer id) {
        newsRepository.deleteById(id);
    }

    @Override
    public String ckeditorUpload(MultipartFile image) {
        String imageName = "";
        String uploadDir = "news/photos/";
        if (!Objects.equals(image.getOriginalFilename(), "")) {
            imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
        }
        try {
            FileUploadUtil.saveFile(uploadDir, imageName, image);

            //Save image in gallery
            Images gallery = new Images(imageName, '/'+uploadDir+imageName );
            System.out.println(gallery);
            imagesRepository.save(gallery);
        } catch (IOException e) {
            System.out.println("You need to add a news image.");
        }
        return uploadDir+imageName;
    }

    @Override
    public List<News> findByMainImage(String mainImage) {
        return newsRepository.findByMainImage(mainImage);
    }

    @Override
    public Page<News> findAllWithFilters(String searchValue, String statusFilter, String newsTypeFilter, String userFilter, Integer idFilter, Pageable pageable, Boolean chapChinFilter) {

        if ((searchValue == null || searchValue.trim().isEmpty()) &&
                (statusFilter == null || statusFilter.trim().isEmpty()) &&
                (newsTypeFilter == null || newsTypeFilter.trim().isEmpty()) &&
                (userFilter == null || userFilter.trim().isEmpty()) &&
                (idFilter == null) &&
                (chapChinFilter == null)){
            return newsRepository.findAll(pageable);
        } else {
            return newsRepository.findByFilters(searchValue, statusFilter, newsTypeFilter, userFilter, idFilter, chapChinFilter,pageable);
        }
    }

    @Transactional
    public List<News> searchNews(String search, String status) {
        List<News> allNews = newsRepository.findAll();

        if (status != null && !status.isBlank()) {
            List<News> filteredByStatus = new ArrayList<>();
            for (News news : allNews) {
                if (news.getStatus().equals(status)) {
                    filteredByStatus.add(news);
                }
            }
            allNews.retainAll(filteredByStatus);
        }


        if (search != null && !search.isEmpty()) {
            List<News> filteredBySearch = new ArrayList<>();
            for (News news : allNews) {
                if (news.getTitle().equalsIgnoreCase(search)) {
                    filteredBySearch.add(news);
                }
            }
            allNews.retainAll(filteredBySearch);
        }


        List<News> finalNews = allNews.stream().distinct().toList();
        return finalNews;
    }

    @Override
    public List<News> findAllByVideoName(String name) {
        return newsRepository.findAllByMainVideo(name);
    }

    @Override
    public List<News> findByMainVideo(String oldVideoName) {
        return newsRepository.findByMainVideo(oldVideoName);
    }

    @Override
    public Page<News> searchNews(String search, Pageable pageable) {
        if (search == null || search.isEmpty()) {
            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
            return newsRepository.findAllByGetTranslatedTrue(sortedPageable);
        } else {
            return newsRepository.findAllByTitleContainingAndGetTranslatedTrue(search, pageable);
        }
    }

    @Override
    public List<News> getBreakingNews() {
        return newsRepository.findByBreakingNewsTrue();
    }

    @Override
    public List<News> getNewsHeadline() {
        // Implement based on your business logic
        // This might fetch news suitable for headlines
        // For example:
        return newsRepository.findByNewsHeadlineTrue();
    }

    @Override
    public List<News> getMostViewed() {
        // Implement based on your business logic
        // This might fetch news ordered by view count
        // For example:
        return newsRepository.findByShowMostViewedTrue();
    }

    @Override
    public List<News> getSliderNews() {
        // Implement based on your business logic
        // This might fetch news suitable for slider (featured news)
        // For example:
        return newsRepository.findBySliderTrue();
    }

    @Override
    public List<News> getLatestNews() {
        return newsRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Override
    public List<News> getTickerNews() {
        return newsRepository.findTop3ByOrderByCreatedAtDesc();
    }

    @Override
    public List<News> getTwoNews() {
        return newsRepository.findTop2ByOrderByCreatedAtDesc();
    }

    @Override
    public void updateCreatedAtById(Integer id, LocalDateTime createdAt) {
        newsRepository.updateCreatedAtById(id,createdAt);
    }

    @Override
    public List<News> findByUserId(Users user) {
        return newsRepository.findByUserId(user);
    }
}
