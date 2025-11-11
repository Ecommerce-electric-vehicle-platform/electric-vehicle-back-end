package Green_trade.green_trade_platform.service;

import java.util.Map;

public interface RedisOtpService {
    void savePending(String username, String email, String otp);

    Map<String, String> getPending(String email);

    void deletePending(String email);

    void savePendingBuyer(String username, String password, String email, String otp);

    Map<String, String> getPendingBuyer(String email);

    void deletePendingBuyer(String email);
}

