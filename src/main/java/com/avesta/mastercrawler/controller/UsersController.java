package com.avesta.mastercrawler.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("admin/users")
public class UsersController {

    @GetMapping("/admins/list")
    public String adminsList() {
        return "users/admin-list";
    }


}
