package com.elearning.elearning_platform.user.infrastructure.config;

import com.elearning.elearning_platform.user.domain.model.Role;
import com.elearning.elearning_platform.user.domain.model.User;
import com.elearning.elearning_platform.user.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataSeederTest {

    private final UserRepositoryPort userRepository = mock(UserRepositoryPort.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    @Test
    void shouldNotCreateAdminWhenPasswordIsNotSet() {
        new DataSeeder(userRepository, passwordEncoder, "").run();

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldCreateAdminWithConfiguredPassword() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret123")).thenReturn("$2a$10$HashedValue1");

        new DataSeeder(userRepository, passwordEncoder, "Secret123").run();

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertEquals("$2a$10$HashedValue1", saved.getValue().getPassword());
        assertEquals(Role.ADMIN, saved.getValue().getRole());
    }
}
