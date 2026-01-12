package ch.tbz.users.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.tbz.users.dto.UserRequest;
import ch.tbz.users.dto.UserResponse;
import ch.tbz.users.entities.User;
import ch.tbz.users.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        UserResponse response = userService.getUserProfile(currentUser.getId(), currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers(@AuthenticationPrincipal User currentUser) {
        List<UserResponse> users = userService.getAllUsers(currentUser);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id, 
            @Valid @RequestBody UserRequest userRequest,
            @AuthenticationPrincipal User currentUser) {
        UserResponse userResponse = userService.updateUser(id, userRequest, currentUser);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> activateUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        UserResponse userResponse = userService.setUserActive(id, true, currentUser);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> deactivateUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        UserResponse userResponse = userService.setUserActive(id, false, currentUser);
        return ResponseEntity.ok(userResponse);
    }
}
