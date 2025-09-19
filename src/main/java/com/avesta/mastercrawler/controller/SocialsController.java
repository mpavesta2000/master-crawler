package com.avesta.mastercrawler.controller;

import com.avesta.mastercrawler.model.SocialAccount;
import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.service.ISocialAccountService;
import com.avesta.mastercrawler.utility.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/socials")
@RequiredArgsConstructor
@Slf4j
public class SocialsController {

    private final ISocialAccountService socialAccountService;

    @GetMapping("")
    public String socialsHome(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Users currentUser = userDetails.getUser();

        List<SocialAccount> userAccounts = socialAccountService.findByUser(currentUser);
        List<SocialAccount> globalAccounts = socialAccountService.findGlobalAccounts();

        model.addAttribute("userAccounts", userAccounts);
        model.addAttribute("globalAccounts", globalAccounts);
        model.addAttribute("platforms", SocialAccount.SocialPlatform.values());
        
        return "socials/socials-home";
    }

    @GetMapping("/x")
    public String xSettings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Users currentUser = userDetails.getUser();

        List<SocialAccount> xAccounts = socialAccountService.findByUserAndPlatform(currentUser, SocialAccount.SocialPlatform.X);
        
        model.addAttribute("xAccounts", xAccounts);
        model.addAttribute("platform", "X");
        
        return "socials/socials-x";
    }

    @GetMapping("/telegram")
    public String telegramSettings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Users currentUser = userDetails.getUser();

        List<SocialAccount> telegramAccounts = socialAccountService.findByUserAndPlatform(currentUser, SocialAccount.SocialPlatform.TELEGRAM);
        SocialAccount globalTelegramAccount = socialAccountService.findGlobalAccounts().stream()
                .filter(acc -> acc.getPlatform() == SocialAccount.SocialPlatform.TELEGRAM)
                .findFirst()
                .orElse(null);
        
        model.addAttribute("telegramAccounts", telegramAccounts);
        model.addAttribute("globalAccount", globalTelegramAccount);
        model.addAttribute("platform", "TELEGRAM");
        
        return "socials/socials-telegram";
    }

    @GetMapping("/eitaa")
    public String eitaaSettings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Users currentUser = userDetails.getUser();

        List<SocialAccount> eitaaAccounts = socialAccountService.findByUserAndPlatform(currentUser, SocialAccount.SocialPlatform.EITAA);
        SocialAccount globalEitaaAccount = socialAccountService.findGlobalAccounts().stream()
                .filter(acc -> acc.getPlatform() == SocialAccount.SocialPlatform.EITAA)
                .findFirst()
                .orElse(null);
        
        model.addAttribute("eitaaAccounts", eitaaAccounts);
        model.addAttribute("globalAccount", globalEitaaAccount);
        model.addAttribute("platform", "EITAA");
        
        return "socials/socials-eitaa";
    }

    @GetMapping("/bale")
    public String baleSettings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Users currentUser = userDetails.getUser();

        List<SocialAccount> baleAccounts = socialAccountService.findByUserAndPlatform(currentUser, SocialAccount.SocialPlatform.BALE);
        SocialAccount globalBaleAccount = socialAccountService.findGlobalAccounts().stream()
                .filter(acc -> acc.getPlatform() == SocialAccount.SocialPlatform.BALE)
                .findFirst()
                .orElse(null);
        
        model.addAttribute("baleAccounts", baleAccounts);
        model.addAttribute("globalAccount", globalBaleAccount);
        model.addAttribute("platform", "BALE");
        
        return "socials/socials-bale";
    }
}
