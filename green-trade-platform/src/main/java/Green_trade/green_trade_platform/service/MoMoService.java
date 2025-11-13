package Green_trade.green_trade_platform.service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface MoMoService {
    Map<String, Object> createPaymentUrl(HttpServletRequest req, long amount) throws Exception;

    Map<String, Object> processReturn(HttpServletRequest request);
}

