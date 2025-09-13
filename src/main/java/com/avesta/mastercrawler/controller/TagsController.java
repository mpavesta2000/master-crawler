package com.avesta.mastercrawler.controller;

import com.avesta.mastercrawler.service.ITagsService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/admin/tags")
public class TagsController {

    private final ITagsService iTagsService;


    @GetMapping("/list")
    public String tagsList(Model model) {
        return "tags/tags-list";
    }

    @PostMapping("/delete/{id}")
    public String deleteTags(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            iTagsService.deleteTag(id);
            redirectAttributes.addFlashAttribute("deleted", true);
        }
        return "redirect:/admin/tags/list";
    }
}
