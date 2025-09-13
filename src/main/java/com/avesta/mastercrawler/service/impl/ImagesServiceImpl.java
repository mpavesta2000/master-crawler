package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.Images;
import com.avesta.mastercrawler.repository.ImagesRepository;
import com.avesta.mastercrawler.service.IImagesService;
import com.avesta.mastercrawler.utility.FileUploadUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class ImagesServiceImpl implements IImagesService {

    private final ImagesRepository imagesRepository;

    public ImagesServiceImpl(ImagesRepository imagesRepository) {
        this.imagesRepository = imagesRepository;
    }

    @Override
    public Images save(Images images) {
        Images image = imagesRepository.save(images);
        return image;
    }

    @Override
    public void imageUpload(MultipartFile image) {

        String imageName = "news_image_" + System.currentTimeMillis() + ".jpg";
        String uploadDir = "news/photos/";
//        if (!Objects.equals(image.getOriginalFilename(), "")) {
//            imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
//        }
        try {
            FileUploadUtil.saveFile(uploadDir, imageName, image);
            Images gallery = new Images(imageName, '/'+uploadDir+imageName );
            System.out.println(gallery);
            imagesRepository.save(gallery);
        } catch (IOException e) {
            System.out.println("You need to add a news image.");
        }
    }

    @Override
    public Page<Images> findAll(Pageable pageable) {
        Pageable sortedByIdDesc = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );
        return imagesRepository.findAll(sortedByIdDesc);
    }

    @Override
    public Page<Images> searchImage(String search, Pageable pageable) {
        if (search == null || search.isEmpty()) {
            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
            return imagesRepository.findAll(sortedPageable);
        } else {
            return imagesRepository.findAllByImageNameContaining(search, pageable);
        }
    }

    @Override
    public void downloadImage(String url) {
        try {
            String uploadDir = "news/photos/";
            Files.createDirectories(Paths.get(uploadDir));

            String imageName = "news_image_" + System.currentTimeMillis() + ".jpg";
            Path imagePath = Paths.get(uploadDir, imageName);

            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, imagePath, StandardCopyOption.REPLACE_EXISTING);
            }

            Images gallery = new Images(imageName, '/'+uploadDir + imageName);
            imagesRepository.save(gallery);

            System.out.println("Image saved successfully: " + gallery);
        } catch (IOException e) {
            System.err.println("Error downloading or saving image: " + url);
            e.printStackTrace();
        }
    }

    @Override
    public boolean isValidImageUrl(String imageUrl) {
        try {
            new URL(imageUrl).toURI();

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Void> response = restTemplate.exchange(
                    imageUrl,
                    HttpMethod.HEAD,
                    null,
                    Void.class
            );

            String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            return contentType != null && contentType.startsWith("image/");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Optional<Images> findById(Integer imageId) {
        return imagesRepository.findById(imageId);
    }

    @Override
    public void delete(Images foundImage) {
        imagesRepository.delete(foundImage);
    }

    @Override
    public List<Images> findAllByImageName(String name) {
        return imagesRepository.findAllByImageName(name);
    }
}
