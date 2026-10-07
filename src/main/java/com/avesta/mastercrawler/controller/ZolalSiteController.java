package com.avesta.mastercrawler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The public Zolal site (templates/zolal). These pages are open to everyone —
 * the matching paths are listed in SecurityConfig — while the CMS stays under
 * /admin. Content is still the static design sample; nothing here reads the
 * database yet.
 *
 * `activeNav` marks the current item in the menu: home, gallery or videos.
 */
@Controller
public class ZolalSiteController {

    @GetMapping("/")
    public String home(Model model) {
        return page(model, "index", "home");
    }

    @GetMapping("/news")
    public String news(Model model) {
        return page(model, "news", null);
    }

    @GetMapping("/news/gallery")
    public String newsGallery(Model model) {
        return page(model, "news-gallery", "gallery");
    }

    @GetMapping("/news/video")
    public String newsVideo(Model model) {
        return page(model, "news-video", "videos");
    }

    @GetMapping("/gallery")
    public String gallery(Model model) {
        return page(model, "gallery", "gallery");
    }

    @GetMapping("/videos")
    public String videos(Model model) {
        return page(model, "videos", "videos");
    }

    @GetMapping("/archive")
    public String archive(Model model) {
        return page(model, "archive", null);
    }

    @GetMapping("/search")
    public String search(Model model) {
        return page(model, "search", null);
    }

    @GetMapping("/about")
    public String about(Model model) {
        return page(model, "about", null);
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        return page(model, "contact", null);
    }

    @GetMapping("/corrections")
    public String corrections(Model model) {
        return page(model, "corrections", null);
    }

    @GetMapping("/principles")
    public String principles(Model model) {
        return page(model, "principles", null);
    }

    @GetMapping("/advertise")
    public String advertise(Model model) {
        return page(model, "advertise", null);
    }

    private String page(Model model, String template, String activeNav) {
        model.addAttribute("activeNav", activeNav);
        return "zolal/" + template;
    }
}
