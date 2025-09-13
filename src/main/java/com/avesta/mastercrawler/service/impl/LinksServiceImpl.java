package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.Images;
import com.avesta.mastercrawler.model.Links;
import com.avesta.mastercrawler.repository.ImagesRepository;
import com.avesta.mastercrawler.repository.LinksRepository;
import com.avesta.mastercrawler.service.ILinksService;
import com.avesta.mastercrawler.utility.FileUploadUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class LinksServiceImpl implements ILinksService {

    private final LinksRepository linksRepository;
    private final ImagesRepository imagesRepository;

    public Links pictureUpload(MultipartFile image, Links link) {
        String imageName = "";
        String uploadDir = "images/news/photos/";

        if (!Objects.equals(image.getOriginalFilename(), "")) {
            imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
            link.setImage("/images/news/photos/" + imageName);
        }
        try {
            FileUploadUtil.saveFile(uploadDir, imageName, image);
            Images gallery = new Images(imageName, '/'+uploadDir+imageName );
            imagesRepository.save(gallery);
        } catch (IOException e) {
            System.out.println("You need to add a links image.");
        }
        return link;
    }

    public Links save(Links link) {
        return linksRepository.save(link);
    }

    public List<Links> findAll() {
        return linksRepository.findAll();
    }

    public Optional<Links> findById(Integer id) {
        return linksRepository.findById(id);
    }
}
