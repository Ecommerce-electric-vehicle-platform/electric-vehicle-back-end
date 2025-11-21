package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.request.UploadPostContentAISupportRequest;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.GeminiServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
public class AIServiceController {

    private final GeminiServiceImpl geminiService;
    private final ResponseMapper responseMapper;

    public AIServiceController(GeminiServiceImpl geminiService, ResponseMapper responseMapper) {
        this.geminiService = geminiService;
        this.responseMapper = responseMapper;
    }

    @PostMapping("/content-upload-post-description")
    public ResponseEntity<RestResponse<?, ?>> suggestContentUploadPostDescription(
            @ModelAttribute UploadPostContentAISupportRequest request,
            @RequestPart("pictures") List<MultipartFile> files
    ) throws Exception {
        String content = geminiService.suggestPostDescription(request, files);

        RestResponse response = responseMapper.toDto(
                true,
                "CONTENT SUGGESTION AI SUPPORTED SUCCESSFULLY",
                content,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }
}
