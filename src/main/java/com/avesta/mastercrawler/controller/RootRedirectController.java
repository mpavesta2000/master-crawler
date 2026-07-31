package com.avesta.mastercrawler.controller;

import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.service.IUsersService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class RootRedirectController {

    private final IUsersService iUsersService;

    @GetMapping("/")
    public String rootRedirect() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = iUsersService.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("user not found."));
        if(user.getUserTypeId().getUserTypeName().equals("Ai") || user.getUserTypeId().getUserTypeName().equals("Admin")) {
            return "redirect:/admin/full-users/report";
        }else {
            return "redirect:/admin/user/report";
        }
    }
}
