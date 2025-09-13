package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.Videos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface IVideosService {
    List<Videos> findAll();
    void videoUpload(MultipartFile video);
    List<Videos> findAllByVideoName(String name);
    void delete(Videos foundVideo);
    void save(Videos video);
    Optional<Videos> findById(Integer videoId);
    Page<Videos> searchVideo(String search, Pageable pageable);
}
