package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.ShippingPartnerMapper;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.model.ShippingPartner;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.response.ShippingPartnerResponse;
import Green_trade.green_trade_platform.service.implement.ShippingPartnerServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/shipping-partner")
@Tag(name = "Shipping Partner", description = "APIs for retrieving shipping partner information")
public class ShippingPartnerController {

    private final ShippingPartnerServiceImpl shippingPartnerService;
    private final ShippingPartnerMapper shippingPartnerMapper;
    private final ResponseMapper responseMapper;

    public ShippingPartnerController(ShippingPartnerServiceImpl shippingPartnerService, ShippingPartnerMapper shippingPartnerMapper, ResponseMapper responseMapper) {
        this.shippingPartnerService = shippingPartnerService;
        this.shippingPartnerMapper = shippingPartnerMapper;
        this.responseMapper = responseMapper;
    }

    @Operation(
            summary = "Get all shipping partners",
            description = """
                    Retrieve a complete list of all active shipping partners integrated with the platform.
                    
                    ## Workflow:
                    1. System queries database for all registered shipping partners
                    2. Each partner record is mapped to standardized response format
                    3. Returns complete list of available shipping partners
                    
                    ## Response Includes:
                    - Partner ID and name
                    - Contact information (email, hotline)
                    - Physical address
                    - Website URL
                    - Creation and update timestamps
                    
                    ## Use Cases:
                    - **Buyers**: Select preferred shipping carrier during checkout
                    - **Sellers**: View available logistics partners for order fulfillment
                    - **Admins**: Manage and monitor shipping partner integrations
                    - **Frontend**: Populate shipping partner dropdown/selection UI
                    
                    ## Shipping Partners:
                    Common partners include:
                    - GHN (Giao Hàng Nhanh)
                    - GHTK (Giao Hàng Tiết Kiệm)
                    - Viettel Post
                    - Other integrated logistics providers
                    
                    ## Security:
                    - Public endpoint - No authentication required
                    - Can be accessed by all users (buyers, sellers, admins)
                    """,
            tags = {"Shipping Partner Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipping partners retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "FETCH SHIPPING PARTNER SUCCESSFULLY",
                                              "data": [
                                                {
                                                  "email": "contact@ghn.vn",
                                                  "partnerName": "Giao Hàng Nhanh (GHN)",
                                                  "address": "123 Đường ABC, Quận 1, TP.HCM",
                                                  "websiteUrl": "https://ghn.vn",
                                                  "hotLine": "1900-1234",
                                                  "createdAt": "2024-01-01T00:00:00",
                                                  "updatedAt": "2024-11-10T10:00:00"
                                                },
                                                {
                                                  "email": "contact@ghtk.vn",
                                                  "partnerName": "Giao Hàng Tiết Kiệm (GHTK)",
                                                  "address": "456 Đường XYZ, Quận 2, TP.HCM",
                                                  "websiteUrl": "https://ghtk.vn",
                                                  "hotLine": "1900-5678",
                                                  "createdAt": "2024-01-01T00:00:00",
                                                  "updatedAt": "2024-11-10T10:00:00"
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
                                              "message": "Failed to fetch shipping partners",
                                              "data": null,
                                              "error": "Internal server error"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/partners")
    public ResponseEntity<RestResponse<List<ShippingPartnerResponse>, Object>> getShippingPartners() {
        List<ShippingPartnerResponse> responseData = new ArrayList<>();
        List<ShippingPartner> shippingPartners = shippingPartnerService.getShippingPartners();
        shippingPartners.forEach(
                shippingPartner -> responseData.add(shippingPartnerMapper.toDto(shippingPartner))
        );
        RestResponse<List<ShippingPartnerResponse>, Object> response = responseMapper.toDto(
                true,
                "FETCH SHIPPING PARTNER SUCCESSFULLY",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }
}
