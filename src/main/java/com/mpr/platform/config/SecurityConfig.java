package com.mpr.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails customer = User.builder()
                .username("customer")
                .password(passwordEncoder.encode("password"))
                .roles("CUSTOMER")
                .build();

        UserDetails analyst = User.builder()
                .username("analyst")
                .password(passwordEncoder.encode("password"))
                .roles("ANALYST")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("password"))
                .roles("ADMINISTRATOR")
                .build();

        return new InMemoryUserDetailsManager(customer, analyst, admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Static resources and health endpoints
                .requestMatchers("/actuator/**", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                // Home page and general catalog view (controller handles role-based view filtering)
                .requestMatchers("/", "/plans", "/plans/{id:[0-9]+}", "/api/plans", "/api/plans/{id:[0-9]+}").permitAll()
                // Plan creation: Strictly reserved for ANALYST and ADMINISTRATOR
                .requestMatchers("/plans/new", "/api/plans").hasAnyRole("ANALYST", "ADMINISTRATOR")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .defaultSuccessUrl("/plans", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // Allow REST API testing without CSRF token
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
