package com.noway.service;

import com.noway.entity.Role;
import com.noway.entity.User;
import com.noway.exception.BadRequestException;
import com.noway.exception.UserAlreadyExistsException;
import com.noway.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_encodesPasswordNormalizesEmailAndSaves() {
        when(userRepository.existsByEmail("buyer@noway.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = authService.register("Buyer One", "Buyer@Noway.com", "password123", "password123", "BUYER");

        assertEquals(Role.BUYER, user.getRole());
        assertEquals("buyer@noway.com", user.getEmail());
        assertEquals("$2a$10$encodedHash", user.getPassword());
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_duplicateEmail_throwsUserAlreadyExists() {
        when(userRepository.existsByEmail("dup@noway.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register("Dup", "dup@noway.com", "password123", "password123", "BUYER"));
    }

    @Test
    void register_passwordMismatch_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> authService.register("A", "a@noway.com", "password123", "different", "BUYER"));
    }

    @Test
    void register_shortPassword_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> authService.register("A", "a@noway.com", "short", "short", "BUYER"));
    }

    @Test
    void register_adminRoleRejected_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> authService.register("A", "a@noway.com", "password123", "password123", "ADMIN"));
    }

    @Test
    void register_invalidRole_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> authService.register("A", "a@noway.com", "password123", "password123", "SUPERUSER"));
    }
}
