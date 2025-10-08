package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.exception.ProfileNotFoundException;
import Green_trade.green_trade_platform.mapper.SellerMapper;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.repository.SellerRepository;
import Green_trade.green_trade_platform.request.UpgradeRequest;
import Green_trade.green_trade_platform.response.KycResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;


@Service
@Slf4j
public class KycService {
    @Autowired
    private BuyerRepository buyerRepository;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private SellerMapper sellerMapper;

    @Value("${api-key}")
    private String fptApiKey;
    @Value(("${api-key-Face}"))
    private String faceApiKey;
    @Value(("${api-key-secret}"))
    private String faceApiSecret;


    public KycResponse verify(Long buyerId, MultipartFile identityFrontImageUrl,
                              MultipartFile businessLicenseUrl, MultipartFile selfieImageUrl,
                              MultipartFile identityBackImageUrl, MultipartFile storePolicyUrl, UpgradeRequest request) throws IOException {
        // Get buyer information who want to upgrade account
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found not found"));

        log.info(">>> Buyer's full name: {}", buyer.getFullName());

        // Check buyer profile
        if("Not have yet".equalsIgnoreCase(buyer.getFullName())) {
            throw new ProfileNotFoundException("Complete your profile to upgrade to a seller account.");
        }

        // Validate file storage before upload into Cloudinary
        validateFile(identityFrontImageUrl);
        validateFile(businessLicenseUrl);
        validateFile(selfieImageUrl);
        validateFile(identityBackImageUrl);
        validateFile(storePolicyUrl);

        // Upload file into Cloudinary
        String frontImageUrl = cloudinaryService.upload(identityFrontImageUrl, "sellers/" + buyerId + ":" + buyer.getUsername() + "/identity_front_image");
        String license = cloudinaryService.upload(identityFrontImageUrl, "sellers/" + buyerId + ":" + buyer.getUsername() + "/business_license_image");
        String backImageUrl = cloudinaryService.upload(identityFrontImageUrl, "sellers/" + buyerId + ":" + buyer.getUsername() + "/identity_back_image");
        String selfieUrl = cloudinaryService.upload(identityFrontImageUrl, "sellers/" + buyerId + ":" + buyer.getUsername() + "/selfie_image");
        String policyUrl = cloudinaryService.upload(identityFrontImageUrl, "sellers/" + buyerId + ":" + buyer.getUsername() + "/policy_image");

        // Check data validation
        Map<String, String> identityData = callOcrApi(frontImageUrl);
        String name = identityData.get("name");
        String idNumber = identityData.get("id_number");

        if(!idNumber.equalsIgnoreCase(request.getIdentityNumber())) {
            return new KycResponse(false, "Document number mismatch", "REJECTED", "ID number not match");
        }

        // Check face
        boolean isMatchFace = callFaceCompareApi(frontImageUrl, selfieUrl);
        if(!isMatchFace) {
            return new KycResponse(false, "Face not matched", "REJECTED", "Face verification failed");
        }

        Seller seller = sellerMapper.toEntity(request, buyer, frontImageUrl, license, backImageUrl, selfieUrl, policyUrl);
        sellerRepository.save(seller);

        return new KycResponse(true, "KYC verified successfully", "VERIFIED", null);

    }

    private void validateFile(MultipartFile file) {
        if(file == null || file.isEmpty())
            throw new IllegalArgumentException("File does not exist.");

        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File " + file.getOriginalFilename() + " is too large.");
        }

        String contentType = file.getContentType();
        if (!List.of("image/jpeg", "image/png").contains(contentType)) {
            throw new IllegalArgumentException("Only JPEG or PNG allowed.");
        }
    }

    private Map<String, String> callOcrApi(String imageUrl) throws IOException {
        URL url = new URL("https://api.fpt.ai/vision/idr/v1.0");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("api-key", fptApiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = "{ \"url\": \"" + imageUrl + "\" }";

        try(OutputStream os = conn.getOutputStream()) {
            os.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        // Parse JSON bằng Jackson
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = mapper.readValue(response.toString(), Map.class);

        String name = (String) result.get("name");
        String idNumber = (String) result.get("id_number");
        return Map.of("name", name, "id_number", idNumber);
    }

    private boolean callFaceCompareApi(String idImageUrl, String selfieUrl) throws IOException {
        URL url = new URL("https://api-us.faceplusplus.com/facepp/v3/compare");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        String data = "api_key=" + faceApiKey +
                "&api_secret=" + faceApiSecret +
                "&image_url1=" + idImageUrl +
                "&image_url2=" + selfieUrl;

        // Gửi dữ liệu POST
        try(OutputStream os = conn.getOutputStream()) {
            os.write(data.getBytes(StandardCharsets.UTF_8));
        }

        // Đọc response
        StringBuilder response = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        // Parse JSON bằng Jackson
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = mapper.readValue(response.toString(), Map.class);

        // Lấy confidence
        double confidence = ((Number) result.get("confidence")).doubleValue();
        return confidence > 80; // Ngưỡng match 80%
    }


}
