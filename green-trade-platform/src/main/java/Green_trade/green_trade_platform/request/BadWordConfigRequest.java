package Green_trade.green_trade_platform.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadWordConfigRequest {
    @NotEmpty(message = "Bad words list cannot be empty.")
    private List<String> badWords;
}

