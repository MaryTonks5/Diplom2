package api.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCredentials {
    private String email;
    private String password;

    public static UserCredentials fromUser(User user) {
        return new UserCredentials(user.getEmail(), user.getPassword());
    }
}
