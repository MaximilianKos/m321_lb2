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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Returns the profile information of the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content)
    })
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal @Parameter(hidden = true) User currentUser) {
        UserResponse response = userService.getUserProfile(currentUser.getId(), currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Returns a list of all users (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required",
            content = @Content)
    })
    public ResponseEntity<List<UserResponse>> getAllUsers(@AuthenticationPrincipal @Parameter(hidden = true) User currentUser) {
        List<UserResponse> users = userService.getAllUsers(currentUser);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates user information (Users can update themselves, Admins can update any user)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - Cannot update other users",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content)
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User ID") @PathVariable UUID id, 
            @Valid @RequestBody UserRequest userRequest,
            @AuthenticationPrincipal @Parameter(hidden = true) User currentUser) {
        UserResponse userResponse = userService.updateUser(id, userRequest, currentUser);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate user", description = "Activates a user account (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User activated successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content)
    })
    public ResponseEntity<UserResponse> activateUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @AuthenticationPrincipal @Parameter(hidden = true) User currentUser) {
        UserResponse userResponse = userService.setUserActive(id, true, currentUser);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user", description = "Deactivates a user account (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deactivated successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content)
    })
    public ResponseEntity<UserResponse> deactivateUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @AuthenticationPrincipal @Parameter(hidden = true) User currentUser) {
        UserResponse userResponse = userService.setUserActive(id, false, currentUser);
        return ResponseEntity.ok(userResponse);
    }
}
