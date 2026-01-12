package ch.tbz.users.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import ch.tbz.users.dto.UserRequest;
import ch.tbz.users.dto.UserResponse;
import ch.tbz.users.entities.User;
import ch.tbz.users.exceptions.EmailAlreadyExistsException;
import ch.tbz.users.exceptions.UnauthorizedException;
import ch.tbz.users.exceptions.UserNotFoundException;
import ch.tbz.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private User getUserById(UUID userId) {
        log.debug("Fetching user by ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private void validateUserAccess(User requestingUser, UUID targetUserId) {
        boolean isAdmin = requestingUser.getRole() == User.Role.ADMIN;
        boolean isOwner = requestingUser.getId().equals(targetUserId);
        
        if (!isAdmin && !isOwner) {
            log.warn("Unauthorized access attempt by user {} to user {}", 
                    requestingUser.getId(), targetUserId);
            throw new UnauthorizedException("You can only access your own profile");
        }
    }

    public List<UserResponse> getAllUsers(User requestingUser) {
        log.info("Admin {} is fetching all users", requestingUser.getId());
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse updateUser(UUID targetUserId, UserRequest userRequest, User requestingUser) {
        log.info("User {} is updating user {}", requestingUser.getId(), targetUserId);
        
        validateUserAccess(requestingUser, targetUserId);
        User targetUser = getUserById(targetUserId);

        if (StringUtils.hasText(userRequest.getEmail()) && 
            !userRequest.getEmail().equals(targetUser.getEmail())) {
            validateEmailUniqueness(userRequest.getEmail());
            targetUser.setEmail(userRequest.getEmail());
            log.debug("Updated email for user {}", targetUserId);
        }

        if (StringUtils.hasText(userRequest.getPassword())) {
            if (userRequest.getPassword().length() < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters");
            }
            targetUser.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
            log.debug("Updated password for user {}", targetUserId);
        }

        userRepository.save(targetUser);
        log.info("Successfully updated user {}", targetUserId);
        return toUserResponse(targetUser);
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("Email already in use: " + email);
        }
    }

    public UserResponse getUserProfile(UUID targetUserId, User requestingUser) {
        log.debug("User {} is fetching profile of user {}", requestingUser.getId(), targetUserId);
        
        validateUserAccess(requestingUser, targetUserId);
        User targetUser = getUserById(targetUserId);
        
        return toUserResponse(targetUser);
    }

    public UserResponse setUserActive(UUID targetUserId, boolean active, User requestingUser) {
        log.info("Admin {} is setting user {} active status to {}", 
                requestingUser.getId(), targetUserId, active);
        
        User targetUser = getUserById(targetUserId);
        targetUser.setActive(active);
        userRepository.save(targetUser);
        
        log.info("Successfully {} user {}", active ? "activated" : "deactivated", targetUserId);
        return toUserResponse(targetUser);
    }
}