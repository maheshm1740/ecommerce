package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.AuthResponse;
import com.ecommerce.backend.dto.LoginRequest;
import com.ecommerce.backend.dto.RegisterRequest;
import com.ecommerce.backend.exception.BusinessException;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.security.JwtUtil;
import com.ecommerce.backend.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;

    @InjectMocks AuthServiceImpl authService;

    // ── shared fixtures ──────────────────────────────────────────────────
    private static final String EMAIL    = "john@example.com";
    private static final String NAME     = "John";
    private static final String RAW_PASS = "secret123";
    private static final String ENC_PASS = "$2a$10$encoded";
    private static final String TOKEN    = "jwt.token.here";

    private User savedUser() {
        return User.builder()
                .id(1L).name(NAME).email(EMAIL).password(ENC_PASS).build();
    }

    // ── register ─────────────────────────────────────────────────────────
    @Nested @DisplayName("register()")
    class Register {

        @Test // ✅ HAPPY PATH
        @DisplayName("should save user and return token when email is new")
        void register_success() {
            RegisterRequest req = new RegisterRequest(NAME, EMAIL, RAW_PASS);

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(RAW_PASS)).thenReturn(ENC_PASS);
            when(userRepository.save(any(User.class))).thenReturn(savedUser());
            when(jwtUtil.generateToken(EMAIL)).thenReturn(TOKEN);

            AuthResponse resp = authService.register(req);

            assertThat(resp.token()).isEqualTo(TOKEN);
            assertThat(resp.email()).isEqualTo(EMAIL);
            assertThat(resp.name()).isEqualTo(NAME);

            verify(passwordEncoder).encode(RAW_PASS);   // password must be encoded
            verify(userRepository).save(any(User.class));
        }

        @Test // ❌ ERROR CASE
        @DisplayName("should throw BusinessException(CONFLICT) when email already exists")
        void register_duplicateEmail_throwsConflict() {
            RegisterRequest req = new RegisterRequest(NAME, EMAIL, RAW_PASS);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(savedUser()));

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already registered")
                    .extracting(e -> ((BusinessException) e).getStatus())
                    .isEqualTo(HttpStatus.CONFLICT);

            verify(userRepository, never()).save(any());   // no save should happen
        }
    }

    // ── login ─────────────────────────────────────────────────────────────
    @Nested @DisplayName("login()")
    class Login {

        @Test // ✅ HAPPY PATH
        @DisplayName("should return token when credentials are valid")
        void login_success() {
            LoginRequest req = new LoginRequest(EMAIL, RAW_PASS);

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(savedUser()));
            when(passwordEncoder.matches(RAW_PASS, ENC_PASS)).thenReturn(true);
            when(jwtUtil.generateToken(EMAIL)).thenReturn(TOKEN);

            AuthResponse resp = authService.login(req);

            assertThat(resp.token()).isEqualTo(TOKEN);
            assertThat(resp.email()).isEqualTo(EMAIL);
        }

        @Test // ❌ WRONG EMAIL
        @DisplayName("should throw BusinessException(UNAUTHORIZED) when email not found")
        void login_unknownEmail_throws401() {
            LoginRequest req = new LoginRequest("ghost@x.com", RAW_PASS);
            when(userRepository.findByEmail("ghost@x.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getStatus())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test // ❌ WRONG PASSWORD
        @DisplayName("should throw BusinessException(UNAUTHORIZED) when password is wrong")
        void login_wrongPassword_throws401() {
            LoginRequest req = new LoginRequest(EMAIL, "wrongpass");

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(savedUser()));
            when(passwordEncoder.matches("wrongpass", ENC_PASS)).thenReturn(false);

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getStatus())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);

            verify(jwtUtil, never()).generateToken(any()); // no token on failure
        }
    }
}