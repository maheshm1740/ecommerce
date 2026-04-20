package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest  // uses H2 in-memory; no full Spring context needed
@DisplayName("UserRepository")
class UserRepositoryTest {

    @Autowired UserRepository userRepository;

    @Test // ✅
    @DisplayName("findByEmail() should return user when email exists")
    void findByEmail_found() {
        User saved = userRepository.save(
                User.builder().email("a@b.com").password("hashed").name("Alice").build());

        Optional<User> result = userRepository.findByEmail("a@b.com");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
    }

    @Test // ❌
    @DisplayName("findByEmail() should return empty when email not found")
    void findByEmail_notFound() {
        Optional<User> result = userRepository.findByEmail("ghost@none.com");
        assertThat(result).isEmpty();
    }

    @Test // ❌ CONSTRAINT
    @DisplayName("should throw exception when saving duplicate email")
    void save_duplicateEmail_throwsException() {
        userRepository.save(
                User.builder().email("dup@x.com").password("h").name("A").build());

        assertThatThrownBy(() -> userRepository.saveAndFlush(
                User.builder().email("dup@x.com").password("h").name("B").build()))
                .isInstanceOf(Exception.class); // DataIntegrityViolationException
    }
}