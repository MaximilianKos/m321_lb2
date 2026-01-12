package ch.tbz.users.services;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ch.tbz.users.dto.LoginRequest;
import ch.tbz.users.dto.RegisterRequest;
import ch.tbz.users.dto.TokenResponse;
import ch.tbz.users.dto.ValidationResponse;
import ch.tbz.users.entities.User;
import ch.tbz.users.entities.User.Role;
import ch.tbz.users.exceptions.EmailAlreadyExistsException;
import ch.tbz.users.exceptions.InactiveUserException;
import ch.tbz.users.exceptions.InvalidTokenException;
import ch.tbz.users.exceptions.UserNotFoundException;
import ch.tbz.users.repositories.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TokenResponse register(@Valid RegisterRequest registerRequest) {
        log.info("Attempting to register new user with email: {}", registerRequest.getEmail());
        
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            log.warn("Registration failed - email already exists: {}", registerRequest.getEmail());
            throw new EmailAlreadyExistsException("Email already registered: " + registerRequest.getEmail());
        }

        User user = User.builder()
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        userRepository.save(user);
        log.info("Successfully registered new user: {}", user.getId());

        String token = jwtService.generateToken(user);
        return new TokenResponse(token);
    }

    public TokenResponse login(@Valid LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", loginRequest.getEmail());
                    return new BadCredentialsException("Invalid email or password");
                });

        if (!user.isActive()) {
            log.warn("Login failed - inactive user: {}", loginRequest.getEmail());
            throw new InactiveUserException("Your account is not active. Please contact support.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), 
                            loginRequest.getPassword()));
        } catch (BadCredentialsException e) {
            log.warn("Login failed - invalid credentials for: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        log.info("Successfully logged in user: {}", user.getId());
        return new TokenResponse(token);
    }

    public ValidationResponse validateToken(String token) {
        log.debug("Validating token");
        
        try {
            if (!jwtService.isTokenValid(token)) {
                log.warn("Token validation failed - token expired");
                throw new InvalidTokenException("Token has expired");
            }

            UUID userId = jwtService.extractClaim(token, 
                    claims -> UUID.fromString(claims.get("userId", String.class)));
            
            if (userId == null) {
                log.warn("Token validation failed - no userId in token");
                throw new InvalidTokenException("Invalid token format");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("Token validation failed - user not found: {}", userId);
                        return new UserNotFoundException("User not found");
                    });

            log.debug("Token validated successfully for user: {}", userId);
            return ValidationResponse.builder()
                    .userId(userId)
                    .role(user.getRole())
                    .build();
        } catch (ExpiredJwtException e) {
            log.warn("Token validation failed - expired JWT");
            throw new InvalidTokenException("Token has expired");
        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token validation failed with unexpected error", e);
            throw new InvalidTokenException("Invalid token");
        }
    }
}
