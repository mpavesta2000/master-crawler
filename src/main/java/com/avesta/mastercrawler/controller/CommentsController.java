package com.avesta.mastercrawler.controller;

import com.avesta.mastercrawler.model.Comments;
import com.avesta.mastercrawler.model.News;
import com.avesta.mastercrawler.service.ICommentsService;
import com.avesta.mastercrawler.service.INewsService;
import com.avesta.mastercrawler.service.IUserProfileService;
import com.avesta.mastercrawler.utility.DataScope;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.Date;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/admin/comments")
public class CommentsController {

    private final ICommentsService iCommentsService;
    private final IUserProfileService iUserProfileService;
    private final INewsService iNewsService;

    @PostMapping("/save/{id}")
    public String addComment(@PathVariable("id") Integer id, Comments comment, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Optional<News> newsOptional = iNewsService.findById(id);
            // Ai users may only comment on their own news
            if (newsOptional.isPresent() && DataScope.canAccess(newsOptional.get())) {
                News news = newsOptional.get();

                Comments newComment = new Comments();
                newComment.setEmail(comment.getEmail());
                newComment.setContent(comment.getContent());
                newComment.setStatus(false);
                newComment.setName(comment.getName());
                newComment.setNews(news);
                iCommentsService.save(newComment);
                redirectAttributes.addFlashAttribute("success", true);
            }
        }
        return "redirect:/admin/news/show/" + id;
    }

    @GetMapping("/list")
    public String commentsList(Model model) {
        return "news/news-comments-list";
    }

    @PostMapping("/delete/{id}")
    public String deleteComments(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            iCommentsService.deleteById(id);
            redirectAttributes.addFlashAttribute("deleted", true);
        }
        return "redirect:/admin/comments/list";
    }

    @PostMapping("/changeStatus/{id}")
    public String changeStatus(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Optional<Comments> comment = iCommentsService.findById(id);
            if (comment.isPresent()) {
                comment.get().setStatus(!comment.get().isStatus());
                iCommentsService.save(comment.get());
                redirectAttributes.addFlashAttribute("changed", true);
            }
        }
        return "redirect:/admin/comments/list";
    }

}
