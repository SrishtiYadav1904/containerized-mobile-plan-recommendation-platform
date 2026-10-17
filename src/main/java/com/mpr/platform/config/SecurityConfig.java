package com.mpr.platform.config;

import com.mpr.platform.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserService userService;

    public SecurityConfig(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Static resources and health check
                .requestMatchers("/actuator/**", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                // Public landing, registration, and catalogue browsing
                .requestMatchers("/", "/register", "/login", "/plans", "/plans/{id:[0-9]+}",
                                 "/recommendations", "/api/auth/**", "/api/plans", "/api/plans/{id:[0-9]+}",
                                 "/api/recommendations").permitAll()
                // Dashboard and plan management operations strictly for Analyst and Administrator
                .requestMatchers("/plans/new", "/plans/{id:[0-9]+}/edit", "/plans/{id:[0-9]+}/status",
                                 "/dashboard", "/api/dashboard/**").hasAnyRole("ANALYST", "ADMINISTRATOR")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/plans", false)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // Allow testing REST APIs without CSRF token
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
