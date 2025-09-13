package com.avesta.mastercrawler.controller;

import com.avesta.mastercrawler.model.UserProfile;
import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.service.IUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final IUsersService iUsersService;

    @Autowired
    public GlobalControllerAdvice(IUsersService iUsersService) {
        this.iUsersService = iUsersService;
    }

    @ModelAttribute("userProfile")
    public UserProfile addUserProfileToModel() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Optional<Users> user = iUsersService.findByEmail(authentication.getName());
            if (user.isPresent()) {
                return user.get().getUserProfile();
            }
        }
        return null;
    }
}
