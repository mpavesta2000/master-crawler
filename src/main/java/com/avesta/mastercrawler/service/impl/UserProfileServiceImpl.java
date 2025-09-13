package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.UserProfile;
import com.avesta.mastercrawler.repository.UserProfileRepository;
import com.avesta.mastercrawler.service.IUserProfileService;
import com.avesta.mastercrawler.utility.FileUploadUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserProfileServiceImpl implements IUserProfileService {

    private final UserProfileRepository userProfileRepository;;


    @Override
    public void save(UserProfile userProfile) {
        userProfileRepository.save(userProfile);
    }

    @Override
    public Optional<UserProfile> findByUserId(Integer users) {
        return userProfileRepository.findById(users);
    }

    @Override
    public UserProfile pictureUpload(MultipartFile image, UserProfile userProfile) {
        String imageName = "";
        String uploadDir = "photos/profile/";

        if (userProfile.getProfilePhoto() != null && !userProfile.getProfilePhoto().isEmpty()) {
            String existingPhotoPath = userProfile.getProfilePhoto().replace("/photos/profile/", "");
            File existingFile = new File(uploadDir + existingPhotoPath);
            if (existingFile.exists()) {
                boolean deleted = existingFile.delete();
                if (!deleted) {
                    System.out.println("Failed to delete the existing profile image: " + existingPhotoPath);
                }
            }
        }
        if (!Objects.equals(image.getOriginalFilename(), "")) {
            imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
            userProfile.setProfilePhoto("/photos/profile/" + imageName);
        }
        try {
            FileUploadUtil.saveFile(uploadDir, imageName, image);
        } catch (IOException e) {
            System.out.println("You need to add a profile image.");
        }
        return userProfile;
    }

    @Override
    public Optional<UserProfile> findByEmail(String name) {
        return userProfileRepository.findByEmail(name);
    }


}
