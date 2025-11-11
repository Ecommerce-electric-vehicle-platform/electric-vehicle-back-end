package Green_trade.green_trade_platform.service;

public interface RedisTokenService {
    void saveTokenToRedis(String email, String token, long expireTime);

    boolean verifyRefreshToken(String email);

    String getRefreshToken(String email);

    void deleteRefreshToken(String email);
}

