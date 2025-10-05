package com.avesta.mastercrawler.controller;

import com.avesta.mastercrawler.model.*;
import com.avesta.mastercrawler.service.IUserProfileService;
import com.avesta.mastercrawler.service.IUsersService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/admin/profile")
@AllArgsConstructor
public class UserProfileController {

    private final IUsersService iUsersService;
    private final IUserProfileService iUserProfileService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/settings")
    public String profileSetting(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof  AnonymousAuthenticationToken)) {
            Optional<Users> user = iUsersService.findByEmail(authentication.getName());
            if (user.isPresent()) {
                Optional<UserProfile> userProfile = iUserProfileService.findByUserId(user.get().getUserId());
               if(userProfile.isPresent()) {
                   UserProfile passedUserProfile = userProfile.get();
                   model.addAttribute("userProfile", passedUserProfile);
               }
            }
        }
        return "users/profile-settings";
    }

    @PostMapping("/password/change")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword, RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof  AnonymousAuthenticationToken)) {
            Optional<Users> user = iUsersService.findByEmail(authentication.getName());
            if (user.isPresent()) {
                if(Objects.equals(newPassword, confirmPassword) && !Objects.equals(newPassword, oldPassword)) {
                    user.get().setPassword(passwordEncoder.encode(newPassword));
                    iUsersService.savePassword(user.get());
                    redirectAttributes.addFlashAttribute("successMessage", "رمز شما با موفقیت ذخیره شد.");
                }else {
                    redirectAttributes.addFlashAttribute("errorMessage", "رمز قدیمی یا تایید رمز را اشتباه وارد کردید.");
                }
            }
        }
        return "redirect:/admin/profile/settings";
    }


}
