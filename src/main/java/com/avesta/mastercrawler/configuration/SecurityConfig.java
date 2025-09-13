package com.avesta.mastercrawler.configuration;


import com.avesta.mastercrawler.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Autowired
    public SecurityConfig(CustomUserDetailsService customUserDetailsService , CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler, CustomAuthenticationFailureHandler customAuthenticationFailureHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
    }

    private final String[] publicUrl = {
            "/admin/authentication/**",
            "/css/**",
            "/assets/**",
            "/fonts/**",
            "/js/**",
            "/json/**",
            "/images/**",
            "/lang/**",
            "/libs/**",
            "/*.images",
            "/*.css",
            "/*.js",
            "/*.js.map",
            "/resources/**",
            "/webjars/**",
            "/admin/change/password",
            "/admin/change/password/save",
            "/error",
            "/api/news/find"
    };

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authenticationProvider(authenticationProvider());
        http.authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(publicUrl).permitAll()
                                .anyRequest().authenticated())
                .formLogin(form->
                        form.loginPage("/admin/authentication/login").permitAll()
                                .successHandler(customAuthenticationSuccessHandler)
                                .failureHandler(customAuthenticationFailureHandler))
                .logout(logout -> {
                    logout.logoutUrl("/admin/authentication/logout");
                    logout.logoutSuccessUrl("/admin/authentication/login");
                }).cors(Customizer.withDefaults())
                .csrf(csrf-> csrf.disable())
                .exceptionHandling(configurer ->
                        configurer
                                .accessDeniedPage("/access-denied")
                );

        http.headers(headers -> {
            headers.frameOptions(frame -> frame.sameOrigin());
            headers.contentSecurityPolicy(csp -> csp.policyDirectives("frame-ancestors 'self' http://127.0.0.1:8080"));
        });

        return  http.build();
    }

    @Bean
    protected AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(customUserDetailsService);
        return authenticationProvider;
    }





}
