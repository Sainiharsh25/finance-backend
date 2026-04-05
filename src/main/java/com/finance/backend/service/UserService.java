package com.finance.backend.service;

import com.finance.backend.dto.request.CreateUserRequest;
import com.finance.backend.dto.request.UpdateUserRequest;
import com.finance.backend.dto.response.UserResponse;
import com.finance.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(Long id);
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    void deactivateUser(Long id);
    User getCurrentUser();
}
