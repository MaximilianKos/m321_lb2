package ch.tbz.users.dto;

import ch.tbz.users.entities.User.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private String email;
    private String password;
    private Role role;
    private Boolean active;
}
