package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.Links;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ILinksService {
    Links pictureUpload(MultipartFile image, Links link);
    Links save(Links link);
    List<Links> findAll();
    Optional<Links> findById(Integer id);
}
