package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ConversationMapper;
import Green_trade.green_trade_platform.mapper.MessageMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.request.MessageRequest;
import Green_trade.green_trade_platform.response.ConversationResponse;
import Green_trade.green_trade_platform.response.MessageResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/chatting")
@AllArgsConstructor
@Tag(name = "Chatting Management", description = "APIs for managing conversations and messages between buyers and sellers")
public class ChattingController {
    private NotificationSocketController socketController;
    private final ConversationServiceImpl conversationService;
    private final BuyerServiceImpl buyerService;
    private final PostProductServiceImpl postProductService;
    private final ConversationMapper conversationMapper;
    private final ResponseMapper responseMapper;
    private final ChattingSocketController chattingSocketController;
    private final MessageMapper messageMapper;
    private final MessageServiceImpl messageService;
    private final SellerServiceImpl sellerService;

    @Operation(summary = """
            Create a new conversation between the buyer and the seller of a post
            One buyer just can create one conversation with a post.
            """, description = """
            This endpoint allows an authenticated **buyer** to initiate a new conversation
            with the **seller** who owns the specified product post (`PostProduct`).
            
            - The buyer must be logged in.
            - The `postId` must correspond to an existing and active post.
            - A conversation will only be created **if it does not already exist** between the buyer and seller for this post.
            - Once created, the conversation can be used to exchange chat messages.
            
            **Use case:**
            Buyers use this API to start chatting with the seller about a specific product they are interested in.
            """)
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @PostMapping("/create-conversation/{postId}")
    public ResponseEntity<?> createConversation(@PathVariable(name = "postId") long id) {
        try {
            log.info(">>> [Chatting Controlelr] Create Conversation: Started.");
            Buyer buyer = buyerService.getCurrentUser();
            PostProduct postProduct = postProductService.findPostProductById(id);

            if (buyer.getSeller() == postProduct.getSeller()) {
                throw new IllegalArgumentException("Sellers cannot create conversation with themselves.");
            }

            log.info(">>> [Chatting Controller] Buyer: {}, Post Product: {}", buyer, postProduct);
            Conversation conversation = conversationMapper.toEntity(buyer, postProduct);
            conversation = conversationService.createConversation(conversation);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "CREATE CONVERSATION SUCCESSFULLY.",
                    conversationMapper.toDto(conversation), null));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "CREATE CONVERSATION FAILED.",
                    null, e.getMessage()));
        }
    }

    @Operation(summary = "Retrieve all conversations of the current buyer", description = """
            This API retrieves all conversations belonging to the currently logged-in buyer.
            The system identifies the current user through the authentication context (session or token)
            and fetches all related conversation records.
            
            - If successful: returns a list of conversations for the buyer.
            - If failed: returns an error message with details in the `message` field.
            """)
    @GetMapping("/conversation")
    public ResponseEntity<?> getConversation() {
        try {
            Buyer buyer = buyerService.getCurrentUser();
            List<Conversation> conversations = conversationService.getConversation(buyer);
            List<ConversationResponse> result;

            // Nếu buyer chưa có conversation
            if (conversations.isEmpty()) {
                Seller seller = buyer.getSeller();
                List<PostProduct> postProducts = sellerService.getListPostProduct(seller);
                List<Conversation> sellerConversations = new ArrayList<>();

                for (PostProduct product : postProducts) {
                    if (product.getConversations() != null && !product.getConversations().isEmpty()) {
                        sellerConversations.addAll(product.getConversations());
                    }
                }

                // Map sang DTO
                result = conversationMapper.toDtoList(sellerConversations);
            } else {
                // Map sang DTO
                result = conversationMapper.toDtoList(conversations);
            }

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET CONVERSATION SUCCESSFULLY.",
                    result, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET CONVERSATION FAILED.",
                    null, e.getMessage()));
        }
    }

    @Operation(summary = "Send a new chat message (text or image)", description = """
            This API creates and sends a new message within an existing conversation
            between a buyer and a seller.
            
            The message can be of two types:
            - **Text message:** provided in the `content` field.
            - **Image message:** uploaded through the `picture` field as multipart form data.
            
            Once the message is successfully created, it will be:
            1. Saved in the database (associated with the given conversation).
            2. Broadcast in real-time via WebSocket using `ChattingSocketController`,
               allowing the receiver to receive it instantly.
            
            **Notes:**
            - The sender and receiver are determined automatically based on the current logged-in user.
            - Either `content` or `picture` must be provided.
            - This endpoint consumes `multipart/form-data`.
            
            **Example use cases:**
            - A buyer sends a text or image to the seller of a product post.
            - A seller replies with a message or image in the same conversation.
            """)
    @PostMapping("/create-message")
    public ResponseEntity<?> createMessage(
            @ModelAttribute MessageRequest request,
            @RequestPart(name = "picture", required = false) MultipartFile picture) throws IOException {
        log.info(">>> [Chatting Controller] Create message: Started.");
        try {
            Message message = messageMapper.toEntity(request);
            log.info(">>> [Chatting Controller] Message: {}", message);

            Buyer currentUser = buyerService.getCurrentUser();
            log.info(">>> [Chatting Controller] Current User: {}", currentUser);

            Buyer buyer = buyerService.findBuyerById(request.getBuyerId());
            log.info(">>> [Chatting Controller] Buyer: {}", buyer.getUsername());

            Seller seller = postProductService.findSellerByPostId(request.getPostId());
            log.info(">>> [Chatting Controller] Seller: {}", seller.getSellerName());

            Conversation conversation = conversationService.findById(request.getConversationId());
            log.info(">>> [Chatting Controller] Conversation: {}", conversation.getId());

            message.setConversation(conversation);

            // Determine sender and receiver
            if (currentUser == buyer) {
                message.setSenderId(buyer.getBuyerId());
                message.setReceiverId(seller.getBuyer().getBuyerId());
            } else {
                message.setSenderId(seller.getBuyer().getBuyerId());
                message.setReceiverId(buyer.getBuyerId());
            }

            Message savedMessage = null;
            // Determine what type of message
            if (picture != null) {
                savedMessage = messageService.handleImageMessage(message, picture);
            } else {
                message.setContent(request.getContent());
                savedMessage = messageService.handleTextmessage(message);
            }
            log.info(">>> [Chatting Controller] Saved message: {}", savedMessage);

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "SEND MESSAGE SUCCESSFULLY.",
                    messageMapper.toDto(savedMessage), null));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "SEND MESSAGE FAILED.",
                    null, e.getMessage()));
        }
    }

    @Operation(
            summary = "Get list of messages in a conversation",
            description = """
                    Retrieve all messages from a specific conversation with pagination support.
                    
                    **Workflow:**
                    1. System validates conversation exists
                    2. Retrieves paginated messages from the conversation
                    3. Returns messages sorted by creation date (newest first)
                    
                    **Query Parameters:**
                    - `conversationId` (Long, required): ID of the conversation
                    - `page` (int, optional): Page number (0-based), default: 0
                    - `size` (int, optional): Number of items per page, default: 10
                    
                    **Response Includes:**
                    - List of messages with content, sender, receiver, timestamps
                    - Message type (TEXT or IMAGE)
                    - Image URLs for image messages
                    - Pagination metadata
                    
                    **Use Cases:**
                    - Loading chat history in conversation view
                    - Paginated message loading for performance
                    - Viewing conversation messages
                    
                    **Security:**
                    - Requires authentication (ROLE_BUYER or ROLE_SELLER)
                    - Users can only view messages from their own conversations
                    """,
            tags = {"Chatting Management"},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Messages retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Not authorized to view this conversation"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conversation not found"
            )
    })
    @GetMapping("/conversation-messages")
    public ResponseEntity<?> getListMessage(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Conversation ID to get messages from", required = true, example = "1")
            @RequestParam(name = "conversationId", required = true) long id
    ) {
        try {
            Conversation conversation = conversationService.findById(id);
            Page<Message> messages = messageService.getConversationMessages(page, size, conversation);
            Page<MessageResponse> response = messages.map(messageMapper::toDto);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET MESSAGES OF CONVERSATION SUCCESSFULLY.",
                    response, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET MESSAGES OF CONVERSATION FAILED.",
                    null, e.getMessage()
            ));
        }
    }
}
