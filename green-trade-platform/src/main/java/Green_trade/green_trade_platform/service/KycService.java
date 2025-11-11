package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.request.UpgradeAccountRequest;
import Green_trade.green_trade_platform.response.KycResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface KycService {
    KycResponse verify(
            MultipartFile identityFrontImageUrl,
            MultipartFile businessLicenseUrl,
            MultipartFile selfieImageUrl,
            MultipartFile identityBackImageUrl,
            MultipartFile storePolicyUrl,
            UpgradeAccountRequest request
    ) throws IOException;

    Map<String, String> callOcrApi(String imageUrl) throws IOException;

    Map<String, String> callOcrApi(MultipartFile file) throws IOException;

    KycResponse update(
            String storeName,
            MultipartFile businessLicense,
            MultipartFile storePolicy
    ) throws IOException;
}

