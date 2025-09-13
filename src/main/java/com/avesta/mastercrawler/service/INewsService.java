package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface INewsService {

    News save(News news);
    Optional<News> findById(Integer id);
    List<String> fileManagerImages();
    Page<News> findAll(Pageable pageable);
    void deleteById(Integer id);
    String ckeditorUpload(MultipartFile image);
    List<News> findByMainImage(String mainImage);
    Page<News> findAllWithFilters(String searchValue, String statusFilter, String newsTypeFilter, String userFilter, Integer idFilter, Pageable pageable);
    List<News> searchNews(String search, String status);
    List<News> findAllByVideoName(String name);
    List<News> findByMainVideo(String oldVideoName);
    Page<News> searchNews(String search, Pageable pageable);
    List<News> getBreakingNews();
    List<News> getNewsHeadline();
    List<News> getMostViewed();
    List<News> getSliderNews();
    List<News> getLatestNews();
    List<News> getTickerNews();
    List<News> getTwoNews();
}
