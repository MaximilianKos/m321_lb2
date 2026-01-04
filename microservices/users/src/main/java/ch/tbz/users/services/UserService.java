package ch.tbz.users.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ch.tbz.users.dto.UserRequest;
import ch.tbz.users.dto.UserResponse;
import ch.tbz.users.entities.User;
import ch.tbz.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private User getUserById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
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

    public List<UserResponse> getAllUsers(User requestingUser) {
        // Authorization check done by @PreAuthorize annotation
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse updateUser(UUID targetUserId, UserRequest userRequest, User requestingUser) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = requestingUser.getRole() == User.Role.ADMIN;

        if (!isAdmin && !requestingUser.getId().equals(targetUserId)) {
            throw new RuntimeException("You can only update your own profile");
        }

        if (userRequest.getEmail() != null) {
            targetUser.setEmail(userRequest.getEmail());
        }

        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            targetUser.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        }

        userRepository.save(targetUser);
        return toUserResponse(targetUser);
    }

    public UserResponse getUserProfile(UUID targetUserId, User requestingUser) {
        User targetUser = getUserById(targetUserId);

        boolean isAdmin = requestingUser.getRole() == User.Role.ADMIN;

        if (!isAdmin && !requestingUser.getId().equals(targetUserId)) {
            throw new RuntimeException("You can only view your own profile");
        }

        return toUserResponse(targetUser);
    }

    public UserResponse setUserActive(UUID targetUserId, boolean active, User requestingUser) {
        // Authorization check done by @PreAuthorize annotation
        User targetUser = getUserById(targetUserId);
        targetUser.setActive(active);
        userRepository.save(targetUser);
        return toUserResponse(targetUser);
    }
}