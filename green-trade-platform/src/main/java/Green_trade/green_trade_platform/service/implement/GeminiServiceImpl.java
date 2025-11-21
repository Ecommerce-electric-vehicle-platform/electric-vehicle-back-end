package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.exception.CategoryNotFound;
import Green_trade.green_trade_platform.exception.ThirdPartyAIException;
import Green_trade.green_trade_platform.repository.CategoryRepository;
import Green_trade.green_trade_platform.request.UploadPostContentAISupportRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Slf4j
public class GeminiServiceImpl {

    private final CategoryRepository categoryRepository;
    @Value("${google.gemini.api.key}")
    private String GEMINI_API_KEY;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Sinh mô tả bài đăng dựa trên thông tin form + ảnh sản phẩm.
     */
    public String suggestPostDescription(UploadPostContentAISupportRequest req,
                                         List<MultipartFile> files) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        String prompt = buildPrompt(req);
        log.info(">>> [Gemini] Prompt gửi lên: \n{}", prompt);

        // ====== Tạo parts: trước là ảnh, cuối cùng là text prompt ======
        List<Map<String, Object>> parts = new ArrayList<>();

        // Thêm ảnh
        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                try {
                    String base64 = Base64.getEncoder().encodeToString(file.getBytes());

                    Map<String, Object> inlineData = new HashMap<>();
                    inlineData.put("mime_type", file.getContentType() != null ? file.getContentType() : "image/jpeg");
                    inlineData.put("data", base64);

                    parts.add(Map.of("inline_data", inlineData));
                } catch (Exception e) {
                    log.error(">>> [Gemini] Lỗi đọc file ảnh {}: {}", file.getOriginalFilename(), e.getMessage());
                }
            }
        }

        // Thêm phần text prompt
        parts.add(Map.of("text", prompt));

        // Body đúng format Gemini REST
        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(
                Map.of(
                        "role", "user",
                        "parts", parts
                )
        ));

        // Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", GEMINI_API_KEY);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    GEMINI_URL,
                    entity,
                    String.class
            );

            String responseBody = response.getBody();
            log.info(">>> [Gemini] Response: {}", responseBody);

            return extractTextFromGeminiResponse(responseBody);

        } catch (Exception e) {
            log.error(">>> [Gemini] Lỗi khi gọi Gemini API: {}", e.getMessage(), e);
            throw new ThirdPartyAIException("Không thể kết nối với dịch vụ AI bên thứ ba.", e);
        }
    }

    /**
     * Prompt dùng các field của UploadPostContentAISupportRequest
     */
    private String buildPrompt(UploadPostContentAISupportRequest request) throws Exception {
        Long categoryId = request.getCategoryId();
        String categoryName = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFound()).getName();

        return """
                Bạn là trợ lý viết nội dung cho sàn thương mại điện tử chuyên xe điện, pin và thiết bị đã qua sử dụng.
                
                Hãy viết phần "Mô tả chi tiết" để đăng bán sản phẩm dựa trên thông tin sau:
                
                Tiêu đề: %s
                Thương hiệu: %s
                Model: %s
                Năm sản xuất: %s
                Thời gian đã sử dụng: %s
                Mức độ tình trạng: %s
                Giá bán: %s VND
                
                Kích thước & khối lượng:
                - Dài (cm): %s
                - Rộng (cm): %s
                - Cao (cm): %s
                - Nặng (gram): %s
                
                Phân loại: %s
                
                Viết nội dung theo đúng FORMAT cố định sau (không được thay đổi):
                
                [FORMAT BẮT BUỘC]
                1) Đoạn 1 – Giới thiệu tổng quan (2–3 câu): Tóm tắt sản phẩm, thương hiệu, model, năm sản xuất, cảm nhận chung.
                2) Đoạn 2 – Tình trạng thực tế (2–4 câu): Mô tả độ mới, mức độ sử dụng, trầy xước, pin, vận hành, ngoại hình.
                3) Đoạn 3 – Công năng & lợi ích (2–3 câu): Nêu rõ sản phẩm phù hợp ai, lý do nên mua, điểm mạnh nổi bật.
                4) Đoạn 4 – Thông tin giao dịch (1–2 câu): Địa điểm, lý do bán nếu có, giá trị sản phẩm.
                
                YÊU CẦU BẮT BUỘC:
                - Tuyệt đối KHÔNG sử dụng câu mở đầu như “Dưới đây là…”, “Sau đây là…”, “Xin chào…”.
                - Tuyệt đối KHÔNG dùng câu kết như “Cảm ơn…”, “Hy vọng…”, “Nếu bạn quan tâm…”.
                - Không nhắc lại yêu cầu, không giải thích bạn là AI.
                - Không tiêu đề, không bullet point, không markdown.
                - Không thêm ký tự đặc biệt, emoji hoặc ký tự không cần thiết.
                - Độ dài toàn văn bản khoảng 6–12 câu.
                - Chỉ trả về phần mô tả cuối cùng, không kèm lời dẫn hoặc chú thích.
                """
                .formatted(
                        n(request.getTitle()),
                        n(request.getBrand()),
                        n(request.getModel()),
                        n(request.getManufactureYear()),
                        n(request.getUsedDuration()),
                        n(request.getConditionLevel()),
                        n(request.getPrice()),
                        n(request.getLength()),
                        n(request.getWidth()),
                        n(request.getHeight()),
                        n(request.getWeight()),
                        n(categoryName)
                );
    }


    /**
     * Parse JSON: candidates[0].content.parts[0].text
     */
    private String extractTextFromGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");

            if (!candidates.isArray() || candidates.size() == 0) {
                log.warn(">>> [Gemini] Không tìm thấy candidates trong response");
                throw new ThirdPartyAIException("Dịch vụ AI trả về phản hồi không hợp lệ (missing candidates).");
            }

            JsonNode content = candidates.get(0).path("content");
            JsonNode parts = content.path("parts");

            if (!parts.isArray() || parts.size() == 0) {
                log.warn(">>> [Gemini] Không tìm thấy parts trong content");
                throw new ThirdPartyAIException("Dịch vụ AI trả về phản hồi không hợp lệ (missing parts).");
            }

            String text = parts.get(0).path("text").asText("");
            log.info(">>> [Gemini] Mô tả sinh ra: {}", text);
            return text;

        } catch (Exception e) {
            log.error(">>> [Gemini] Lỗi khi parse JSON response: {}", e.getMessage(), e);
            throw new ThirdPartyAIException("Không thể xử lý phản hồi từ dịch vụ AI bên thứ ba.", e);
        }
    }

    private String n(Object object) {
        return object == null ? "không rõ" : object.toString();
    }
}
