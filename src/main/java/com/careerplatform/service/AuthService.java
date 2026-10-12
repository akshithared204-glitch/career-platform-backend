
package com.careerplatform.service;

import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.careerplatform.entity.User;
import com.careerplatform.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<String, Object> register(
            String fullName, String email, String password) {

        if (fullName == null || fullName.isBlank()
                || email == null || email.isBlank()
                || password == null || password.length() < 8) {

            throw new IllegalArgumentException(
                    "Enter your name and email, and use a password with at least 8 characters.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "This email is already registered.");
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));

        User savedUser = userRepository.save(user);

        return Map.of(
                "message", "Registration successful!",
                "userId", savedUser.getId(),
                "fullName", savedUser.getFullName(),
                "email", savedUser.getEmail()
        );
    }

    public Map<String, Object> login(
            String email, String password) {

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {

            throw new IllegalArgumentException(
                    "Please enter your email and password.");
        }

        User user = userRepository.findByEmail(
                email.trim().toLowerCase()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Invalid email or password."));

        if (!passwordEncoder.matches(
                password, user.getPassword())) {

            throw new IllegalArgumentException(
                    "Invalid email or password.");
        }

        return Map.of(
                "message", "Login successful!",
                "userId", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail()
        );
    }

    public Map<String, Object> register(
            Map<String, String> request) {

        return register(
                request.get("fullName"),
                request.get("email"),
                request.get("password")
        );
    }

    public Map<String, Object> login(
            Map<String, String> request) {

        return login(
                request.get("email"),
                request.get("password")
        );
    }
}
