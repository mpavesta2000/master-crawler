package com.avesta.mastercrawler.controller;

import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.service.IUsersService;
import com.avesta.mastercrawler.service.IUsersTypeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
@AllArgsConstructor
public class AuthenticationController {

    private final IUsersTypeService iUsersTypeService;
    private final IUsersService iUsersService;

    @GetMapping("/admin/authentication/signup")
    public String showSignUp(Model model) {
        model.addAttribute("getAllTypes", iUsersTypeService.findAll());
        model.addAttribute("user", new Users());
        return "authentication/auth-signup-cover";
    }

    @PostMapping("/admin/authentication/signup/save")
    public String saveUser(Users users, Model model) {
        Optional<Users> optionalUser  = iUsersService.findByEmail(users.getEmail());
        if(optionalUser.isPresent()) {
            model.addAttribute("getAllTypes", iUsersTypeService.findAll());
            model.addAttribute("user", new Users());
            return "authentication/auth-signup-cover";
        }else{
            iUsersService.save(users);
            return "authentication/auth-signin-cover";
        }
    }

    @GetMapping("/admin/authentication/login")
    public String login(Model model) {
        return "authentication/auth-signin-cover";
    }

    @GetMapping("/admin/authentication/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return "redirect:/admin/authentication/login";
    }
}
