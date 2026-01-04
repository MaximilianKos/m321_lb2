package ch.tbz.users.services;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ch.tbz.users.dto.LoginRequest;
import ch.tbz.users.dto.RegisterRequest;
import ch.tbz.users.dto.TokenResponse;
import ch.tbz.users.dto.ValidationResponse;
import ch.tbz.users.entities.User;
import ch.tbz.users.entities.User.Role;
import ch.tbz.users.repositories.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TokenResponse register(@Valid RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setActive(true);

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new TokenResponse(token);
    }

    public TokenResponse login(@Valid LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Wrong email or password"));

        if (!user.isActive()) {
            throw new RuntimeException("Your account is not active");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), loginRequest.getPassword()));
        } catch (AuthenticationException e) {
            throw new RuntimeException("Wrong email or password");
        }

        String token = jwtService.generateToken(user);
        return new TokenResponse(token);
    }

    public ValidationResponse validateToken(String token) {
        try {
            if (!jwtService.isTokenValid(token)) {
                throw new RuntimeException("Token expired");
            }

            UUID userId = jwtService.extractClaim(token, claims -> UUID.fromString(claims.get("userId", String.class)));
            if (userId == null) {
                throw new RuntimeException("Invalid token");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Invalid token"));

            return ValidationResponse.builder()
                    .userId(userId)
                    .role(user.getRole())
                    .build();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token expired");
        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
    }
}
