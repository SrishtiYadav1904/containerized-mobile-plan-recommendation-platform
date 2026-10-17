package com.mpr.platform.service;

import com.mpr.platform.model.User;
import com.mpr.platform.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String name, String email, String rawPassword, String role) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        String effectiveRole = (role != null && !role.trim().isEmpty()) ? role.trim().toUpperCase() : "CUSTOMER";
        if (!List.of("CUSTOMER", "ANALYST", "ADMINISTRATOR").contains(effectiveRole)) {
            throw new IllegalArgumentException("Invalid role: " + effectiveRole);
        }

        String passwordHash = passwordEncoder.encode(rawPassword);
        User user = new User(name.trim(), normalizedEmail, passwordHash, effectiveRole);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(email.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new UsernameNotFoundException("Username/Email cannot be empty");
        }

        String target = identifier.trim().toLowerCase();
        // Allow lookup by full email or username alias (e.g. 'analyst' -> 'analyst@example.com')
        User user = userRepository.findByEmailIgnoreCase(target)
                .or(() -> userRepository.findByEmailIgnoreCase(target + "@example.com"))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));

        String roleName = user.getRole().startsWith("ROLE_") ? user.getRole() : "ROLE_" + user.getRole();
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority(roleName))
        );
    }

    @PostConstruct
    public void seedInitialUsersIfEmpty() {
        if (userRepository.count() == 0) {
            registerUser("Customer Demo", "customer@example.com", "password", "CUSTOMER");
            registerUser("Analyst Demo", "analyst@example.com", "password", "ANALYST");
            registerUser("Administrator Demo", "admin@example.com", "password", "ADMINISTRATOR");
        }
    }
}
