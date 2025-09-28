package Green_trade.green_trade_platform.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {
    @NotBlank(message = "username must not be blank.")
    private String username;

    @NotBlank(message = "Password must not be blank")
    private String password;
}
