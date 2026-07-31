package com.avesta.mastercrawler.configuration;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        System.out.println("The user name that logged in is this: " + username);
        boolean hasAdminRole = authentication.getAuthorities().stream().anyMatch(r->r.getAuthority().equals("Admin"));
        boolean hasUserRole = authentication.getAuthorities().stream().anyMatch(r->r.getAuthority().equals("User"));
        boolean hasAiRole = authentication.getAuthorities().stream().anyMatch(r->r.getAuthority().equals("Ai"));

        if (hasAdminRole) {
            System.out.println("Redirecting to /admin/full-users/report");
            httpServletResponse.sendRedirect("/admin/full-users/report");
        } else if (hasUserRole) {
            System.out.println("Redirecting to /admin/user/report");
            httpServletResponse.sendRedirect("/admin/user/report");
        } else if (hasAiRole) {
            System.out.println("Redirecting to /admin/user/report");
            httpServletResponse.sendRedirect("/admin/full-users/report");
        }else {
            System.out.println("No admin or user role detected.");
        }
    }
}
