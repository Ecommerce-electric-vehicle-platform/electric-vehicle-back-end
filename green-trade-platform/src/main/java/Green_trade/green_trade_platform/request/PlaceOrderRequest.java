package Green_trade.green_trade_platform.request;

import Green_trade.green_trade_platform.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {
    private Long postProductId;
    private String username;
    private String shippingAddress;
    private String phoneNumber;
    private Long shippingPartnerId;
    private Long transactionId;
}
