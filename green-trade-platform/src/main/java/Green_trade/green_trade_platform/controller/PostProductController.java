package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.exception.PostProductNotFound;
import Green_trade.green_trade_platform.exception.SubscriptionExpiredException;
import Green_trade.green_trade_platform.mapper.PostProductListMapper;
import Green_trade.green_trade_platform.mapper.PostProductMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.SellerMapper;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.request.UpdatePostProductRequest;
import Green_trade.green_trade_platform.response.PostProductListResponse;
import Green_trade.green_trade_platform.response.PostProductResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.response.SellerResponse;
import Green_trade.green_trade_platform.service.implement.PostProductServiceImpl;
import Green_trade.green_trade_platform.service.implement.SellerServiceImpl;
import Green_trade.green_trade_platform.service.implement.SubscriptionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/post-product")
@RequiredArgsConstructor
@Tag(name = "Product Post", description = "APIs for product listing, searching, and retrieving product information")
public class PostProductController {
    private final PostProductServiceImpl postProductService;
    private final ResponseMapper responseMapper;
    private final PostProductListMapper postProductListMapper;
    private final PostProductMapper postProductMapper;
    private final SellerMapper sellerMapper;
    private final SellerServiceImpl sellerService;

    @Operation(
            summary = "Get all available product posts with pagination and sorting",
            description = """
                    This endpoint retrieves a paginated list of all product posts that are currently available for purchase
                    (i.e., not sold yet). It is typically used on the product listing page of the buyer interface.
                    
                    ## Workflow:
                    1. Client sends request with pagination parameters (page, size)
                    2. System fetches available products (not sold) from database
                    3. Applies sorting based on provided parameters (sort_by, is_asc)
                    4. Returns paginated response with product list and metadata
                    
                    ## Query Parameters:
                    - **page**: Page number (0-based indexing), default: 0
                    - **size**: Number of items per page, default: 10
                    - **sort_by**: Field to sort by (id, createdAt, price, etc.), default: "id"
                    - **is_asc**: Sort direction (true for ascending, false for descending), default: true
                    
                    ## Response Structure:
                    - List of product posts with details (title, brand, model, price, images, etc.)
                    - Pagination metadata (currentPage, totalElements, totalPage)
                    
                    ## Security:
                    - Public endpoint - No authentication required
                    """,
            tags = {"Product Listing"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Products retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Get post product successfully.",
                                              "data": {
                                                "content": [
                                                  {
                                                    "id": 1,
                                                    "title": "iPhone 13 Pro Max",
                                                    "brand": "Apple",
                                                    "model": "iPhone 13 Pro Max",
                                                    "price": 25000000,
                                                    "conditionLevel": "LIKE_NEW",
                                                    "locationTrading": "TP. Hồ Chí Minh",
                                                    "images": ["https://cloudinary.com/image1.jpg"],
                                                    "sold": false,
                                                    "createdAt": "2024-01-15T10:00:00"
                                                  }
                                                ],
                                                "meta": {
                                                  "currentPage": 0,
                                                  "totalElements": 150,
                                                  "totalPage": 15
                                                }
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid parameters",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Get post product failed.",
                                              "data": {
                                                "content": [],
                                                "meta": {}
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("")
    public ResponseEntity<RestResponse<PostProductListResponse, Object>> getAllProduct(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Field to sort by (id, createdAt, price, etc.)", example = "id")
            @RequestParam(name = "sort_by", defaultValue = "id") String sortedBy,
            @Parameter(description = "Sort direction (true=ascending, false=descending)", example = "true")
            @RequestParam(name = "is_asc", defaultValue = "true") boolean isAsc
    ) {
        try {
            Page<PostProduct> postProductPage = postProductService.getAllProductPaging(page, size, sortedBy, isAsc);
            Map<String, Object> meta = Map.of(
                    "currentPage", postProductPage.getNumber(),
                    "totalElements", postProductPage.getTotalElements(),
                    "totalPage", postProductPage.getTotalPages()
            );

            PostProductListResponse responseData = postProductListMapper.toDto(postProductPage.getContent(), meta);

            RestResponse<PostProductListResponse, Object> response = responseMapper.toDto(
                    true,
                    "Get post product successfully.",
                    responseData,
                    null
            );
            return ResponseEntity.status(HttpStatus.OK.value()).body(response);
        } catch (Exception e) {
            PostProductListResponse responseData = postProductListMapper.toDto(new ArrayList<PostProduct>(), Map.of());
            RestResponse<PostProductListResponse, Object> response = responseMapper.toDto(
                    false,
                    "Get post product failed.",
                    responseData,
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
        }
    }

    @Operation(
            summary = "Get seller information by post product ID",
            description = """
                        Retrieves detailed information about the seller associated with a specific post product.
                        
                        ## Workflow:
                        1. System locates the post product using the provided postId
                        2. If found, retrieves the seller linked to that post
                        3. Returns seller details (store name, contact info, verification status)
                        4. If post product not found, throws 404 error
                        
                        ## Use Cases:
                        - Displaying seller information on product detail page
                        - Showing seller ratings and verification status
                        - Allowing buyers to view product owner details
                        
                        ## Security:
                        - Public endpoint - No authentication required
                        - Sensitive seller data should be filtered based on user permissions
                    """,
            tags = {"Product Information"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Seller information retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "FETCH SELLER BY POST SUCCESSFULLY",
                                              "data": {
                                                "sellerId": 1,
                                                "storeName": "ABC Electronics Store",
                                                "phoneNumber": "0912345678",
                                                "email": "seller@example.com",
                                                "address": "123 Đường XYZ, TP.HCM",
                                                "verified": true,
                                                "rating": 4.5,
                                                "totalSales": 150
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Post product not found",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Post product not found",
                                              "data": null,
                                              "error": "PostProduct with ID 123 does not exist"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{postId}/seller")
    public ResponseEntity<RestResponse<SellerResponse, Object>> getSellerByPostId(
            @Parameter(
                    description = "The ID of the post product",
                    required = true,
                    example = "123"
            )
            @PathVariable(name = "postId") Long id
    ) {
        PostProduct postProduct = postProductService.findPostProductById(id);
        if (postProduct == null) {
            throw new PostProductNotFound();
        }
        Seller seller = postProduct.getSeller();
        SellerResponse responseData = sellerMapper.toDto(seller);
        RestResponse<SellerResponse, Object> response = responseMapper.toDto(
                true,
                "FETCH SELLER BY POST SUCCESSFULLY",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @Operation(
            summary = "Get post product information based on a wish-list ID",
            description = """
                    Allows an authenticated buyer or seller to retrieve detailed information 
                    of a product post associated with a specific wish-list item.
                    
                    ## Workflow:
                    1. System validates authentication token
                    2. Locates wish-list entry using wishId
                    3. Retrieves associated PostProduct from wish-list
                    4. Returns complete product information
                    
                    ## Use Cases:
                    - Buyers viewing details of wish-listed items
                    - Sellers checking which posts are in buyer wish lists
                    - Quick access to product details from wish list
                    
                    ## Security:
                    - Requires authentication (ROLE_BUYER or ROLE_SELLER)
                    - Only authenticated users can access wish-list information
                    """,
            tags = {"Wish List Management"},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Post product information retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "GET POST PRODUCT INFORMATION SUCCESSFULLY.",
                                              "data": {
                                                "id": 123,
                                                "title": "Yamaha Exciter 155",
                                                "brand": "Yamaha",
                                                "model": "Exciter 155",
                                                "price": 45000000,
                                                "conditionLevel": "LIKE_NEW",
                                                "locationTrading": "Hà Nội",
                                                "description": "Xe máy còn mới, ít sử dụng",
                                                "images": ["https://cloudinary.com/exciter1.jpg"],
                                                "sold": false,
                                                "createdAt": "2024-01-10T08:00:00"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Request failed (note: current implementation returns 200 even on error)",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Error Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "GET POST PRODUCT INFORMATION FAILED.",
                                              "data": null,
                                              "error": "Wish list entry not found"
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
    @PreAuthorize("hasAnyRole('ROLE_SELLER', 'ROLE_BUYER')")
    @GetMapping("/{wishId}")
    public ResponseEntity<?> getPostInfoByWishId(
            @Parameter(
                    description = "The ID of the wish-list entry",
                    required = true,
                    example = "1"
            )
            @PathVariable(name = "wishId") long id
    ) {
        try {
            PostProduct postProduct = postProductService.findPostByWishId(id);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET POST PRODUCT INFORMATION SUCCESSFULLY.",
                    postProductMapper.toDto(postProduct), null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET POST PRODUCT INFORMATION FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Search post products by type and value",
            description = """
                    Search products based on a specific search type and value with pagination support.
                    
                    ## Workflow:
                    1. Client provides search type and value
                    2. System queries database based on search criteria
                    3. Returns paginated results matching the search
                    
                    ## Supported Search Types:
                    - **title**: Search by product title
                    - **brand**: Search by brand name
                    - **model**: Search by model name
                    - **conditionLevel**: Search by condition (NEW, LIKE_NEW, USED, etc.)
                    - **locationTrading**: Search by trading location
                    
                    ## Query Parameters:
                    - **type**: Search field type (default: "brand")
                    - **value**: Search keyword/value (default: "Pega")
                    - **page**: Page number (0-based), default: 0
                    - **size**: Items per page, default: 10
                    
                    ## Example Requests:
                    - Search by brand: `/api/v1/post-product/search?type=brand&value=Yamaha`
                    - Search by title: `/api/v1/post-product/search?type=title&value=iPhone`
                    - Search with pagination: `/api/v1/post-product/search?type=brand&value=Honda&page=0&size=20`
                    
                    ## Security:
                    - Public endpoint - No authentication required
                    """,
            tags = {"Product Search"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "SEARCH PRODUCT SUCCESSFULLY.",
                                              "data": {
                                                "content": [
                                                  {
                                                    "id": 1,
                                                    "title": "Yamaha Exciter 155",
                                                    "brand": "Yamaha",
                                                    "model": "Exciter 155",
                                                    "price": 45000000,
                                                    "conditionLevel": "LIKE_NEW",
                                                    "locationTrading": "Hà Nội",
                                                    "images": ["https://cloudinary.com/exciter1.jpg"],
                                                    "sold": false,
                                                    "createdAt": "2024-01-10T08:00:00"
                                                  }
                                                ],
                                                "pageable": {
                                                  "pageNumber": 0,
                                                  "pageSize": 10
                                                },
                                                "totalElements": 25,
                                                "totalPages": 3,
                                                "last": false,
                                                "first": true
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Search failed (note: current implementation returns 200 even on error)",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Error Response",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "SEARCH PRODUCT FAILED.",
                                              "data": null,
                                              "error": "Invalid search type or database error"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchProduct(
            @Parameter(
                    description = "Search field type (title, brand, model, conditionLevel, locationTrading)",
                    example = "brand"
            )
            @RequestParam(name = "type", defaultValue = "brand") String type,
            @Parameter(
                    description = "Search keyword/value",
                    example = "Yamaha"
            )
            @RequestParam(name = "value", defaultValue = "Pega") String value,
            @Parameter(
                    description = "Page number (0-based)",
                    example = "0"
            )
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(
                    description = "Number of items per page",
                    example = "10"
            )
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Page<PostProduct> products = postProductService.searchProduct(type, value, page, size);
            Page<PostProductResponse> response = products.map(postProductMapper::toDto);

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "SEARCH PRODUCT SUCCESSFULLY.",
                    response, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "SEARCH PRODUCT FAILED.",
                    null, e.getMessage()
            ));
        }
    }
}
