package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.DisputeCategoryMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.DisputeCategory;
import Green_trade.green_trade_platform.response.DisputeCategoryResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.DisputeCategoryServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dispute-category")
@Tag(name = "Dispute Category", description = "APIs for retrieving dispute categories")
public class DisputeCategoryController {

    private final DisputeCategoryServiceImpl disputeCategoryService;
    private final ResponseMapper responseMapper;
    private final DisputeCategoryMapper disputeCategoryMapper;

    public DisputeCategoryController(DisputeCategoryServiceImpl disputeCategoryService, ResponseMapper responseMapper, DisputeCategoryMapper disputeCategoryMapper) {
        this.disputeCategoryService = disputeCategoryService;
        this.responseMapper = responseMapper;
        this.disputeCategoryMapper = disputeCategoryMapper;
    }

    @Operation(
            summary = "Get all dispute categories",
            description = """
                        Retrieves a list of available dispute categories that users can select when submitting 
                        a complaint, issue, or support request.  
                    
                        **Workflow:**
                        1. The system fetches all active dispute categories from the database.
                        2. Each category includes an ID, name, and description.
                        3. The response returns the full list, which can be displayed in dropdown menus or forms on the frontend.
                    
                        **Use cases:**
                        - Allowing customers to choose a category when opening a dispute (e.g., "Product not delivered", "Damaged item").
                        - Used by admin or customer service tools to classify dispute reports.
                    """,
            tags = {"Dispute Category"},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dispute categories retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "FETCH DISPUTE CATEGORY LIST",
                                              "data": [
                                                {
                                                  "categoryId": 1,
                                                  "categoryName": "Product not delivered",
                                                  "description": "Order was not received by customer"
                                                },
                                                {
                                                  "categoryId": 2,
                                                  "categoryName": "Damaged item",
                                                  "description": "Product arrived in damaged condition"
                                                },
                                                {
                                                  "categoryId": 3,
                                                  "categoryName": "Wrong product",
                                                  "description": "Received different product than ordered"
                                                }
                                              ],
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient permissions"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @GetMapping("/dispute-categories")
    public ResponseEntity<RestResponse<List<DisputeCategoryResponse>, Object>> getAllDisputeCategory() {
        List<DisputeCategoryResponse> responseData = new ArrayList<>();
        List<DisputeCategory> disputeCategories = disputeCategoryService.getAllDisputeCategory();
        disputeCategories.forEach(disputeCategory -> responseData.add(disputeCategoryMapper.toDto(disputeCategory)));
        RestResponse<List<DisputeCategoryResponse>, Object> response = responseMapper.toDto(
                true,
                "FETCH DISPUTE CATEGORY LIST",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }
}
