package com.noway.service;

import com.noway.dto.RegisterRequest;
import com.noway.entity.Role;
import com.noway.entity.User;
import com.noway.exception.BadRequestException;
import com.noway.exception.UserAlreadyExistsException;
import com.noway.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        return register(request.name(), request.email(), request.password(),
                request.confirmPassword(), request.role());
    }

    public User register(String name, String email, String rawPassword,
                         String confirmPassword, String role) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Name is required.");
        }
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required.");
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new BadRequestException("Email must be a valid email address.");
        }
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters.");
        }
        if (confirmPassword == null || !rawPassword.equals(confirmPassword)) {
            throw new BadRequestException("Passwords do not match.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("Email is already registered: " + normalizedEmail);
        }

        Role parsedRole;
        try {
            parsedRole = Role.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid role. Choose BUYER or SELLER.");
        }
        if (parsedRole == Role.ADMIN) {
            throw new BadRequestException("You cannot register as ADMIN.");
        }

        User user = new User(name.trim(), normalizedEmail, passwordEncoder.encode(rawPassword), parsedRole);
        return userRepository.save(user);
    }
}
