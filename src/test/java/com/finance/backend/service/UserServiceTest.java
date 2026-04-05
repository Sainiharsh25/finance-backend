package com.finance.backend.service;

import com.finance.backend.dto.request.CreateUserRequest;
import com.finance.backend.dto.request.UpdateUserRequest;
import com.finance.backend.dto.response.UserResponse;
import com.finance.backend.entity.User;
import com.finance.backend.enums.Role;
import com.finance.backend.exception.DuplicateResourceException;
import com.finance.backend.exception.ResourceNotFoundException;
import com.finance.backend.repository.UserRepository;
import com.finance.backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .password("encoded_password")
                .role(Role.ANALYST)
                .active(true)
                .build();
    }

    // --- createUser ---

    @Test
    @DisplayName("createUser: success when email is unique")
    void createUser_Success() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("new@example.com");
        request.setFullName("New User");
        request.setPassword("password123");
        request.setRole(Role.VIEWER);

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u = User.builder()
                    .id(2L).email(u.getEmail()).fullName(u.getFullName())
                    .password(u.getPassword()).role(u.getRole()).active(true).build();
            return u;
        });

        UserResponse response = userService.createUser(request);

        assertThat(response.getEmail()).isEqualTo("new@example.com");
        assertThat(response.getRole()).isEqualTo(Role.VIEWER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser: throws DuplicateResourceException when email already exists")
    void createUser_DuplicateEmail_ThrowsException() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setFullName("Duplicate");
        request.setRole(Role.VIEWER);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("test@example.com");

        verify(userRepository, never()).save(any());
    }

    // --- getUserById ---

    @Test
    @DisplayName("getUserById: returns user when found")
    void getUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("getUserById: throws ResourceNotFoundException when not found")
    void getUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- updateUser ---

    @Test
    @DisplayName("updateUser: updates only provided fields")
    void updateUser_PartialUpdate() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(Role.ADMIN);
        request.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);

        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
        assertThat(response.isActive()).isFalse();
        assertThat(response.getFullName()).isEqualTo("Test User"); // unchanged
    }

    // --- deactivateUser ---

    @Test
    @DisplayName("deactivateUser: sets active to false")
    void deactivateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.deactivateUser(1L);

        assertThat(sampleUser.isActive()).isFalse();
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("deactivateUser: throws when user not found")
    void deactivateUser_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
