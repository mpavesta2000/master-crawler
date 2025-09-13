package com.avesta.mastercrawler.controller;

import com.avesta.mastercrawler.model.Category;
import com.avesta.mastercrawler.model.News;
import com.avesta.mastercrawler.service.ICategoryService;
import com.avesta.mastercrawler.service.INewsService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/admin/category")
public class CategoryController {

    private final ICategoryService iCategoryService;
    private final INewsService iNewsService;

    @GetMapping("/all")
    public String allCategories(Model model) {
        List<Category> parentCategories = iCategoryService.findByParentIsNull();
        List<Category> childCategories = iCategoryService.findByParentIsNotNull();


        model.addAttribute("childCategories", childCategories);
        model.addAttribute("parentCategories", parentCategories);
        model.addAttribute("category", new Category());
        model.addAttribute("categories", iCategoryService.findAll());

        return "categories/categories";
    }

    @PostMapping("/save")
    public String saveCategory(RedirectAttributes redirectAttributes, @ModelAttribute Category category) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {

            if (category.getParent() == null) {
                if (category.getId() != null) {
                    Category existingCategory = iCategoryService.findById(category.getId()).orElse(null);

                    if (existingCategory != null) {
                        existingCategory.setName(category.getName());
                        existingCategory.setStatus(category.getStatus());

                        for (Category child : existingCategory.getChildren()) {
                            child.setParent(existingCategory);
                        }

                        Category savedParentCategory = iCategoryService.save(existingCategory);
                        if (savedParentCategory.getId() != null) {
                            redirectAttributes.addFlashAttribute("success", true);
                        }
                    } else {
                        redirectAttributes.addFlashAttribute("error", "Category not found");
                    }
                } else {
                    Category savedParentCategory = iCategoryService.save(category);
                    if (savedParentCategory.getId() != null) {
                        redirectAttributes.addFlashAttribute("success", true);
                    }
                }
            } else {
                Category savedChildCategory = iCategoryService.save(category);
                if (savedChildCategory.getId() != null) {
                    redirectAttributes.addFlashAttribute("success", true);
                }
            }
        }
        return "redirect:/admin/category/all";
    }

    @GetMapping("/edit/{id}")
    public String editCategory(Model model,@PathVariable("id") Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)) {
            Optional<Category> foundCategory = iCategoryService.findById(id);
            List<Category> parentCategories = iCategoryService.findByParentIsNull();
            List<Category> childCategories = iCategoryService.findByParentIsNotNull();


            model.addAttribute("childCategories", childCategories);
            model.addAttribute("parentCategories", parentCategories);
            model.addAttribute("category", foundCategory);
            model.addAttribute("categories", iCategoryService.findAll());
        }
        return "categories/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Optional<Category> categoryOptional = iCategoryService.findById(id);

            if (categoryOptional.isPresent()) {
                Category category = categoryOptional.get();

                reassignNewsToDefaultCategory(category);

                iCategoryService.deleteById(id);
                redirectAttributes.addFlashAttribute("deleted", true);
            }
        }
        return "redirect:/admin/category/all";
    }

    private void reassignNewsToDefaultCategory(Category category) {
        List<News> associatedNews = category.getNews();

        if (associatedNews != null && !associatedNews.isEmpty()) {
            Category defaultCategory = iCategoryService.defaultCategory();

            for (News news : associatedNews) {
                List<Category> updatedCategories = new ArrayList<>();
                updatedCategories.add(defaultCategory);
                news.setCategories(updatedCategories);
                iNewsService.save(news);
            }
        }

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            for (Category child : category.getChildren()) {
                reassignNewsToDefaultCategory(child);
            }
        }
    }



}
