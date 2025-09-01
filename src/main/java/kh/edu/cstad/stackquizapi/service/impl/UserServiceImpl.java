package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.User;
import kh.edu.cstad.stackquizapi.dto.request.CreateUserRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateUserRequest;
import kh.edu.cstad.stackquizapi.dto.response.UserResponse;
import kh.edu.cstad.stackquizapi.mapper.UserMapper;
import kh.edu.cstad.stackquizapi.repository.UserRepository;
import kh.edu.cstad.stackquizapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        try {
            if (createUserRequest == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User request cannot be null");
            }
            if (createUserRequest.firstName() == null || createUserRequest.firstName().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First name cannot be null or empty");
            }
            if (createUserRequest.lastName() == null || createUserRequest.lastName().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Last name cannot be null or empty");
            }
            if (createUserRequest.email() == null || createUserRequest.email().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email cannot be null or empty");
            }
            if (createUserRequest.password() == null || createUserRequest.password().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password cannot be null or empty");
            }
            if (createUserRequest.password().length() < 8) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
            }

            if (userRepository.existsByEmail(createUserRequest.email().trim())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            }

            User user = userMapper.fromCreateUserRequest(createUserRequest);
            user.setCreatedAt(LocalDateTime.now());
            user.setIsActive(true);
            User savedUser = userRepository.save(user);
            return userMapper.toUserResponse(savedUser);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating user", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occurred");
        }
    }

    @Override
    public UserResponse getUserById(String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID cannot be null or empty");
            }

            return userRepository.findByIdAndIsActiveTrue(userId)
                    .map(userMapper::toUserResponse)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found or inactive"));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching user by id {}", userId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occurred");
        }
    }

    @Override
    public UserResponse updateUserByUserId(String userId, UpdateUserRequest updateUserRequest) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID cannot be null or empty");
            }
            if (updateUserRequest == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Update request cannot be null");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            if (updateUserRequest.email() != null &&
                    !updateUserRequest.email().trim().isEmpty() &&
                    !user.getEmail().equals(updateUserRequest.email().trim()) &&
                    userRepository.existsByEmail(updateUserRequest.email().trim())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            }

            userMapper.toCustomerPartially(updateUserRequest, user);
            user.setUpdatedAt(LocalDateTime.now());

            user = userRepository.save(user);
            return userMapper.toUserResponse(user);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while updating user with id {}", userId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occurred");
        }
    }

    @Override
    public void deleteUserByUserId(String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID cannot be null or empty");
            }

            if (!userRepository.existsById(userId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            user.setIsActive(false);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while deleting user with id {}", userId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occurred");
        }
    }

    @Override
    public List<UserResponse> findAll() {
        try {
            List<User> users = userRepository.findAllByIsActiveTrue();
            return users.stream().map(userMapper::toUserResponse).toList();
        } catch (Exception e) {
            log.error("Unexpected error while fetching users", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occurred");
        }
    }
}
