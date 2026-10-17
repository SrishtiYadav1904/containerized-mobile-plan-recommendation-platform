package com.mpr.platform.service;

import com.mpr.platform.model.User;
import com.mpr.platform.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("registerUser saves user with BCrypt hashed password and specified role")
    void testRegisterUserSuccess() {
        when(userRepository.existsByEmailIgnoreCase("newuser@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        User registered = userService.registerUser("New User", "newuser@example.com", "secret123", "ANALYST");

        assertNotNull(registered);
        assertEquals("ANALYST", registered.getRole());
        assertEquals("newuser@example.com", registered.getEmail());
        assertNotEquals("secret123", registered.getPasswordHash());
        assertTrue(passwordEncoder.matches("secret123", registered.getPasswordHash()));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser rejects duplicate email registration")
    void testRegisterUserDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser("Duplicate", "existing@example.com", "password", "CUSTOMER")
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("loadUserByUsername loads user and prefixes role with ROLE_")
    void testLoadUserByUsername() {
        User user = new User("Test User", "test@example.com", passwordEncoder.encode("password"), "ADMINISTRATOR");
        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR")));
    }

    @Test
    @DisplayName("loadUserByUsername throws UsernameNotFoundException when user does not exist")
    void testLoadUserNotFound() {
        when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userService.loadUserByUsername("unknown@example.com")
        );
    }
}
