package ch.tbz.products.clients;

import ch.tbz.products.dto.ValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "users-service", url = "${users.service.url}")
public interface UsersServiceClient {
    
    @PostMapping(value = "/auth/validate", consumes = MediaType.TEXT_PLAIN_VALUE)
    ValidationResponse validateToken(@RequestBody String token);
}
