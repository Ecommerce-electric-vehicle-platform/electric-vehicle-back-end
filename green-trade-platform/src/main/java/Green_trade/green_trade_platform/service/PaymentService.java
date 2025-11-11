package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.Payment;

public interface PaymentService {
    Payment findPaymentMethodById(Long id);
}

