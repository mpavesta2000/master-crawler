package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.UserProfile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface IUserProfileService {

    void save(UserProfile userProfile);
    Optional<UserProfile> findByUserId(Integer users);
    UserProfile pictureUpload(MultipartFile image, UserProfile userProfile);
    Optional<UserProfile> findByEmail(String name);

}
