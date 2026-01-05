package ch.tbz.products.services;

import ch.tbz.products.clients.UsersServiceClient;
import ch.tbz.products.dto.Role;
import ch.tbz.products.dto.TokenValidationRequest;
import ch.tbz.products.dto.ValidationResponse;
import ch.tbz.products.exceptions.UnauthorizedException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UsersServiceClient usersServiceClient;
    
    public ValidationResponse validateToken(String token) {
        try {
            log.info("Validating token with Users Service");
            
            TokenValidationRequest request = TokenValidationRequest.builder()
                    .token(token)
                    .build();
            
            ValidationResponse response = usersServiceClient.validateToken(request);
            
            log.info("Token validated successfully. UserId: {}, Role: {}", 
                    response.getUserId(), response.getRole());
            
            return response;
            
        } catch (FeignException.Unauthorized e) {
            log.error("Token validation failed: Unauthorized");
            throw new UnauthorizedException("Invalid or expired token");
        } catch (FeignException e) {
            log.error("Error calling Users Service: {}", e.getMessage());
            throw new UnauthorizedException("Unable to validate token");
        }
    }
    
    public ValidationResponse validateAdminToken(String token) {
        ValidationResponse response = validateToken(token);
        
        if (response.getRole() != Role.ADMIN) {
            log.warn("User {} attempted admin action without admin role", response.getUserId());
            throw new UnauthorizedException("Admin access required");
        }
        
        return response;
    }
    
    public String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
        return authorizationHeader.substring(7);
    }
}
