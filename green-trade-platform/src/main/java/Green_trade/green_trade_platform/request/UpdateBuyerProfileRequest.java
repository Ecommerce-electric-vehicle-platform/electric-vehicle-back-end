package Green_trade.green_trade_platform.request;

import Green_trade.green_trade_platform.enumerate.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBuyerProfileRequest {
    private String fullName;
    private String email;
    private Gender gender;
    private LocalDate birthDay;
    private String phoneNumber;
    private String defaultShippingAddress;
}
