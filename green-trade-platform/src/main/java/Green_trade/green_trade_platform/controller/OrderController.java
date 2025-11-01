package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.OrderListMapper;
import Green_trade.green_trade_platform.mapper.OrderMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.ReviewMapper;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.Review;
import Green_trade.green_trade_platform.request.ReviewRequest;
import Green_trade.green_trade_platform.response.OrderListResponse;
import Green_trade.green_trade_platform.response.OrderResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/order")
@Slf4j
@AllArgsConstructor
public class OrderController {

    private final OrderServiceImpl orderService;
    private final ResponseMapper responseMapper;
    private final OrderListMapper orderListMapper;
    private final BuyerServiceImpl buyerService;
    private final GhnServiceImpl ghnService;
    private final OrderMapper orderMapper;
    private final ReviewServiceImpl reviewService;
    private final ReviewMapper reviewMapper;

    @Operation(
            summary = "Get order history of current user",
            description = """
                        Retrieves a paginated list of past orders belonging to the currently authenticated buyer.  
                        The system uses the access token to identify the buyer and fetches their order history, 
                        including details such as order ID, total amount, status, and order date.
                    
                        **Workflow:**
                        1. The frontend sends a request with pagination parameters (`page`, `size`).
                        2. The backend identifies the buyer from the JWT token.
                        3. The system retrieves a paginated list of the buyer’s orders, sorted by date (latest first).
                        4. Pagination metadata (current page, total pages, total elements) is included in the response.
                    
                        **Use cases:**
                        - Displaying a user’s order history in their profile dashboard.
                        - Fetching paginated order records for mobile or web apps.
                    
                        **Security Notes:**
                        - Requires authentication via JWT (`ROLE_BUYER`).
                        - Each user can only access their own order history.
                    """
    )
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @GetMapping("/history")
    public ResponseEntity<RestResponse<OrderListResponse, Object>> getOrdersHistoryOfCurrentUser(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Buyer buyer = buyerService.getCurrentUser();
            Page<Order> orderPaging = orderService.getOrdersOfCurrentUserPaging(size, page, buyer);

            Map<String, Object> meta = Map.of(
                    "currentPage", orderPaging.getNumber(),
                    "totalElements", orderPaging.getTotalElements(),
                    "totalPage", orderPaging.getTotalPages()
            );

            OrderListResponse orderListResponse = orderListMapper.toDto(orderPaging.toList(), meta);

            RestResponse<OrderListResponse, Object> response = responseMapper.toDto(
                    true,
                    "FETCH ORDER HISTORY SUCCESSFULLY",
                    orderListResponse,
                    null
            );

            return ResponseEntity.status(HttpStatus.OK.value()).body(response);
        } catch (Exception e) {
            log.info(">>> Error at getOrdersHistoryOfCurrentUser: {}", e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @Operation(
            summary = "Cancel an order",
            description = """
                        Cancels an existing order for the currently authenticated user (buyer).  
                        This API updates the order status in the system and notifies the external GHN shipping service 
                        to cancel the corresponding shipment.
                    
                        **Workflow:**
                        1. The client sends a `POST` request with the order ID in the URL path.
                        2. The system validates that the order exists and belongs to the authenticated user.
                        3. The order status is updated to `CANCELED`.
                        4. The system calls the GHN shipping service API to cancel the shipping request.
                        5. The updated order information is returned in the response.
                    
                        **Use cases:**
                        - Buyers canceling an order before it is shipped.
                        - Sellers or system administrators canceling orders with failed payments or stock issues.
                        - Synchronizing order cancellations between internal system and GHN shipping API.
                    
                        **Security Notes:**
                        - Requires JWT authentication (either `ROLE_BUYER` or `ROLE_SELLER`).
                        - A user can only cancel orders they own.
                    """
    )
    @PostMapping("/cancel/{id}")
    public ResponseEntity<RestResponse<OrderResponse, Object>> cancelOrder(@PathVariable Long id) throws Exception {
        Order canceledOrder = orderService.cancelOrder(id);
        ghnService.createCancelOrderShippingServiceResponseToDto(canceledOrder.getOrderCode(), canceledOrder.getPostProduct().getSeller().getGhnShopId());
        OrderResponse responseData = orderMapper.toDto(canceledOrder);
        RestResponse<OrderResponse, Object> response = responseMapper.toDto(
                true,
                "CANCELED ORDER SUCCESSFULLY",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @Operation(
            summary = "Create a product review with optional images",
            description = """
                    This endpoint allows customers to create a review for an electrical product they have purchased.
                    
                    The request should include:
                    - **Review details** (order ID, rating, feedback text) as a JSON object named `request`.
                    - **Optional product images** (photos of the product or proof of use) as `pictures`.
                    
                    The API automatically checks the feedback text for inappropriate or offensive language (Vietnamese supported).
                    Uploaded images will be stored on Cloudinary and associated with the review record.
                    
                    **Content type:** multipart/form-data  
                    **Authentication:** Required if the platform uses user accounts.
                    """
    )
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @PostMapping(
            value = "/review",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> createReview(@ModelAttribute ReviewRequest request,
                                          @RequestPart(name = "pictures", required = false) List<MultipartFile> reviewImages) {
        log.info(">>> [Order Controller] Create Review: Started.");
        try {
            Review savedReview = reviewService.createReview(request, reviewImages);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "MAKE REVIEW SUCCESSFULLY.",
                    reviewMapper.toDto(savedReview), null));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "MAKE REVIEW FAILED.",
                    null, e.getMessage()));
        }
    }
}
