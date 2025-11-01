package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ConversationMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Conversation;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.service.implement.BuyerServiceImpl;
import Green_trade.green_trade_platform.service.implement.ConversationServiceImpl;
import Green_trade.green_trade_platform.service.implement.PostProductServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/chatting")
@AllArgsConstructor
public class ChattingController {
    private NotificationSocketController socketController;
    private final ConversationServiceImpl conversationService;
    private final BuyerServiceImpl buyerService;
    private final PostProductServiceImpl postProductService;
    private final ConversationMapper conversationMapper;
    private final ResponseMapper responseMapper;

    @Operation(
            summary = """
                    Create a new conversation between the buyer and the seller of a post 
                    One buyer just can create one conversation with a post.
                    """,
            description = """
                    This endpoint allows an authenticated **buyer** to initiate a new conversation 
                    with the **seller** who owns the specified product post (`PostProduct`).
                    
                    - The buyer must be logged in.
                    - The `postId` must correspond to an existing and active post.
                    - A conversation will only be created **if it does not already exist** between the buyer and seller for this post.
                    - Once created, the conversation can be used to exchange chat messages.
                    
                    **Use case:**  
                    Buyers use this API to start chatting with the seller about a specific product they are interested in.
                    """
    )
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
                    conversationMapper.toDto(conversation), null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "CREATE CONVERSATION FAILED.",
                    null, e.getMessage()
            ));
        }
    }


}
