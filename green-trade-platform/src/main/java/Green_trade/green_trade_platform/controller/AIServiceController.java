package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.request.UploadPostContentAISupportRequest;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.GeminiServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI Content Suggestion", description = "Endpoints for AI-assisted content creation workflows.")
public class AIServiceController {

    private final GeminiServiceImpl geminiService;
    private final ResponseMapper responseMapper;

    public AIServiceController(GeminiServiceImpl geminiService, ResponseMapper responseMapper) {
        this.geminiService = geminiService;
        this.responseMapper = responseMapper;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SELLER')")
    @PostMapping(value = "/content-upload-post-description", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Suggest enriched post description for a listing upload.",
            description = "Uses Gemini to analyze textual listing details together with the uploaded media files "
                    + "and returns an AI-crafted product description ready for publication.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = UploadPostContentAISupportRequest.class),
                            encoding = {
                                    @Encoding(
                                            name = "pictures",
                                            contentType = "image/*"
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "AI suggestion generated successfully.",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or unsupported media content.",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required or token expired.",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "AI service failed or Gemini integration is unreachable.",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Third-party AI vendor is unavailable or returned an invalid response.",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            )
    })
    public ResponseEntity<RestResponse<?, ?>> suggestContentUploadPostDescription(
            @ModelAttribute UploadPostContentAISupportRequest request,
            @Parameter(
                    description = "Product photos (binary). Used by the AI to understand appearance and context.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
                    )
            )
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
