package com.avesta.mastercrawler.configuration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof DisabledException) {
            request.getSession().setAttribute("error", "حساب کاربری شما غیرفعال شده است و نمی‌توانید وارد شوید.");
        } else {
            request.getSession().setAttribute("error", "نام کاربری یا رمز عبور اشتباه است.");
        }
        response.sendRedirect("/admin/authentication/login");
    }
}
