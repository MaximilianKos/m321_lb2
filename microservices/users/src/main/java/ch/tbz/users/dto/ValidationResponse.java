package ch.tbz.users.dto;

import java.util.UUID;

import ch.tbz.users.entities.User.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResponse {
    private UUID userId;
    private Role role;
}
