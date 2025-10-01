package Green_trade.green_trade_platform.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Builder
public class SignInRequest {
    @NotBlank(message = "Username must not be blank.")
    private final String username;
    @NotBlank(message = "Password must not be blank.")
    private final String password;
}
