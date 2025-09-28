package Green_trade.green_trade_platform.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpRequest {
    @NotBlank(message = "Username must not be blank.")
    @Pattern(regexp = "^[a-zA-Z]{8,}$",
            message = "Username must be at least 8 letters, with no spaces, numbers, or special characters.")
    private String username;

    @NotBlank(message = "Password must not be blank.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])[^\\s]{8,}$",
            message = "Password must be at least 8 characters, include letters, numbers, and special characters, and contain no spaces."
    )
    private String password;

    @NotBlank(message = "Email must not be blank.")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Invalid email format."
    )
    private String email;
}
