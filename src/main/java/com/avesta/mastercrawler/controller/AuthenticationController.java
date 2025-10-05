package com.avesta.mastercrawler.controller;

import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.service.IUsersService;
import com.avesta.mastercrawler.service.IUsersTypeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class AuthenticationController {

    private final IUsersTypeService iUsersTypeService;
    private final IUsersService iUsersService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/admin/signup/4f8e3bc28e934dc29b97f3ae9f25a7e1")
    public String adminSignup(Model model) {
        model.addAttribute("getAllTypes", iUsersTypeService.findAll());
        model.addAttribute("user", new Users());
        return "authentication/auth-signup-cover";
    }

    @GetMapping("/admin/login")
    public String adminLogin(Model model) {
        return "authentication/auth-signin-cover";
    }

    @PostMapping("/admin/signup/save")
    public String saveAdmin(Users users, RedirectAttributes redirectAttributes) {
        Optional<Users> optionalUser  = iUsersService.findByEmail(users.getEmail());
        if(optionalUser.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "کاربر با این ایمیل در سیستم وجود دارد");
            return "redirect:/admin/signup/4f8e3bc28e934dc29b97f3ae9f25a7e1";
        }else{
            iUsersService.save(users);
            redirectAttributes.addFlashAttribute("errorMessage", "کاربر با موفقیت ساخته شد");
            return "redirect:/admin/users/admins/list";
        }
    }

    @GetMapping("/admin/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return "redirect:/admin/login";
    }

    @GetMapping("/admin/change/password")
    public String adminChangePassword() {
        return "authentication/auth-pass-reset-basic";
    }

    @PostMapping("/admin/change/password/save")
    public String changePassword(@RequestParam("newPassword") String newPassword,
                                 @RequestParam("email") String email,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 RedirectAttributes redirectAttributes) {

        Optional<Users> user = iUsersService.findByEmail(email);

        if (user.isPresent()) {
            if (Objects.equals(newPassword, confirmPassword)) {
                user.get().setPassword(passwordEncoder.encode(newPassword));
                iUsersService.savePassword(user.get());
                redirectAttributes.addFlashAttribute("successMessage", "رمز شما با موفقیت ذخیره شد.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "رمز قدیمی یا تایید رمز را اشتباه وارد کردید.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "ایمیلی با این مشخصات یافت نشد.");
        }

        return "redirect:/admin/change/password";
    }
}
