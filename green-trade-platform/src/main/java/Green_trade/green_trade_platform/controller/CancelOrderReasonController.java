package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.CancelOrderReasonMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.CancelOrderReason;
import Green_trade.green_trade_platform.response.CancelOrderReasonResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.CancelOrderReasonServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cancel-order-reason")
@Tag(name = "Cancel Order Reason", description = "APIs for retrieving predefined cancel order reasons")
public class CancelOrderReasonController {

    private final CancelOrderReasonServiceImpl cancelOrderReasonService;
    private final CancelOrderReasonMapper cancelOrderReasonMapper;
    private final ResponseMapper responseMapper;

    public CancelOrderReasonController(CancelOrderReasonServiceImpl cancelOrderReasonService, CancelOrderReasonMapper cancelOrderReasonMapper, ResponseMapper responseMapper) {
        this.cancelOrderReasonService = cancelOrderReasonService;
        this.cancelOrderReasonMapper = cancelOrderReasonMapper;
        this.responseMapper = responseMapper;
    }

    @Operation(
            summary = "Get all cancel order reasons",
            description = """
                    Retrieve a complete list of all predefined cancel order reasons available in the system.
                    
                    ## Workflow:
                    1. System queries database for all cancel order reason records
                    2. Each reason is mapped to response DTO format
                    3. Returns complete list of available cancellation reasons
                    
                    ## Response Includes:
                    - Reason ID (unique identifier)
                    - Reason name (description text)
                    
                    ## Use Cases:
                    - **Buyers**: Select reason when canceling an order
                    - **Sellers**: View cancellation reasons for their orders
                    - **Admins**: Monitor cancellation patterns and reasons
                    - **Frontend**: Populate cancellation reason dropdown/selection UI
                    - **Analytics**: Track most common cancellation reasons
                    
                    ## Common Cancel Reasons:
                    Typical reasons include:
                    - Changed mind / No longer need the product
                    - Found better price elsewhere
                    - Shipping address error
                    - Payment issues
                    - Product not as described
                    - Delivery time too long
                    - Other (custom reason)
                    
                    ## Business Rules:
                    - All reasons are predefined by system administrators
                    - Reasons cannot be modified or deleted if associated with existing orders
                    - Each reason has a unique name
                    - Reasons are used for order cancellation tracking and analytics
                    
                    ## Security:
                    - Public endpoint - No authentication required
                    - Can be accessed by all users (buyers, sellers, admins)
                    """,
            tags = {"Order Cancellation Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cancel order reasons retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "FETCH CANCEL ORDER REASONS SUCCESSFULLY",
                                              "data": [
                                                {
                                                  "id": 1,
                                                  "cancelOrderReasonName": "Changed mind / No longer need the product"
                                                },
                                                {
                                                  "id": 2,
                                                  "cancelOrderReasonName": "Found better price elsewhere"
                                                },
                                                {
                                                  "id": 3,
                                                  "cancelOrderReasonName": "Shipping address error"
                                                },
                                                {
                                                  "id": 4,
                                                  "cancelOrderReasonName": "Payment issues"
                                                },
                                                {
                                                  "id": 5,
                                                  "cancelOrderReasonName": "Product not as described"
                                                },
                                                {
                                                  "id": 6,
                                                  "cancelOrderReasonName": "Delivery time too long"
                                                },
                                                {
                                                  "id": 7,
                                                  "cancelOrderReasonName": "Other"
                                                }
                                              ],
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Failed to fetch cancel order reasons",
                                              "data": null,
                                              "error": "Internal server error"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("")
    public ResponseEntity<RestResponse<?, ?>> getAllCancelOrderReasons() {
        List<CancelOrderReason> result = cancelOrderReasonService.getAllCancelOrderReasons();
        List<CancelOrderReasonResponse> responseData = new ArrayList<>();

        result.forEach(cancelOrderReason -> {
            responseData.add(cancelOrderReasonMapper.toDto(cancelOrderReason));
        });
        return ResponseEntity.status(HttpStatus.OK.value()).body(responseMapper.toDto(
                true,
                "FETCH CANCEL ORDER REASONS SUCCESSFULLY",
                responseData,
                null
        ));
    }
}
