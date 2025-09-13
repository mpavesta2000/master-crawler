package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.Videos;
import com.avesta.mastercrawler.repository.VideosRepository;
import com.avesta.mastercrawler.service.IVideosService;
import com.avesta.mastercrawler.utility.FileUploadUtil;
import com.avesta.mastercrawler.utility.VideoConverter;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class VideosServiceImpl implements IVideosService {

    private final VideosRepository videosRepository;

    public List<Videos> findAll() {
        return videosRepository.findAll();
    }

    public void videoUpload(MultipartFile video) {
        String videoName = "";
        if (!Objects.equals(video.getOriginalFilename(), "")) {
            videoName = StringUtils.cleanPath(Objects.requireNonNull(video.getOriginalFilename()));
        }
        String uploadDirVideo = "news/videos/";
        String videoFilePath = uploadDirVideo + videoName;
        String convertedFilePath = uploadDirVideo + "converted_" + videoName;

        try {
            FileUploadUtil.saveFile(uploadDirVideo, videoName, video);

            //Save video in gallery
            Videos videoGallery = new Videos(videoName, '/'+uploadDirVideo+videoName, null);
            System.out.println(videoGallery);
            videosRepository.save(videoGallery);

            VideoConverter.convertToSafariCompatible(videoFilePath, convertedFilePath);

            File originalFile = new File(videoFilePath);
            if (originalFile.exists()) {
                originalFile.delete();
            }
            new File(convertedFilePath).renameTo(new File(videoFilePath));


        } catch (IOException | InterruptedException e) {
            System.out.println("An error occurred while processing the video.");
        }
    }

    public List<Videos> findAllByVideoName(String name) {
        return videosRepository.findAllByVideoName(name);
    }

    public void delete(Videos foundVideo) {
        videosRepository.delete(foundVideo);
    }

    public void save(Videos video) {
        videosRepository.save(video);
    }

    public Optional<Videos> findById(Integer videoId) {
        return videosRepository.findById(videoId);
    }

    public Page<Videos> searchVideo(String search, Pageable pageable) {
        if (search == null || search.isEmpty()) {
            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
            return videosRepository.findAll(sortedPageable);
        } else {
            return videosRepository.findAllByVideoNameContaining(search, pageable);
        }
    }
}
