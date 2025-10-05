package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.UserProfile;
import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.repository.UsersRepository;
import com.avesta.mastercrawler.service.IUsersService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UsersServiceImpl implements IUsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<Users> findByEmail(String email) {
        return usersRepository.findByEmail(email);
    }

    @Override
    public void save(Users user) {
        if (user.getUserProfile() == null) {
            UserProfile userProfile = new UserProfile();
            userProfile.setUserId(user);
            userProfile.setEmail(user.getEmail());
            userProfile.setProfilePhoto("/assets/images/profile-defult.jpg");
            user.setUserProfile(userProfile);
        }

        user.setActive(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        usersRepository.save(user);
    }

    @Override
    public Page<Users> findAllAdmins(Pageable pageable) {
//        return usersRepository.findAllByUserTypeId_UserTypeName("Admin", pageable);
        return usersRepository.findAll(pageable);
    }

    @Override
    public void savePassword(Users users) {
        usersRepository.save(users);
    }

    @Override
    public Optional<Users> findById(Integer id) {
        return usersRepository.findById(id);
    }

    @Override
    public List<Users> findAll() {
        return usersRepository.findAll();
    }

    @Override
    public Page<Users> findAllAdminsWithFilters(String searchValue, Pageable pageable) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return findAllAdmins(pageable);
        } else {
            return usersRepository.findByFilters(searchValue, pageable);
        }
    }
}
