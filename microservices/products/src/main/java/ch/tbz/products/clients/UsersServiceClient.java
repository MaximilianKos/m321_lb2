package ch.tbz.products.clients;

import ch.tbz.products.dto.TokenValidationRequest;
import ch.tbz.products.dto.ValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "users-service", url = "${users.service.url}")
public interface UsersServiceClient {
    
    @PostMapping("/auth/validate")
    ValidationResponse validateToken(@RequestBody TokenValidationRequest request);
}
