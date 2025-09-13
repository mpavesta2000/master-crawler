package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.Images;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface IImagesService {
    Images save(Images images);
    void imageUpload(MultipartFile image);
    Page<Images> findAll(Pageable pageable);
    Page<Images> searchImage(String search, Pageable pageable);
    void downloadImage(String url);
    boolean isValidImageUrl(String imageUrl);
    Optional<Images> findById(Integer imageId);
    void delete(Images foundImage);
    List<Images> findAllByImageName(String name);
}
