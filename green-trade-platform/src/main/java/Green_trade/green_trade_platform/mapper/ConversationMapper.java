package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Conversation;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.response.ConversationResponse;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {
    public Conversation toEntity(Buyer buyer, PostProduct postProduct) {
        return Conversation.builder()
                .buyer(buyer)
                .postProduct(postProduct)
                .build();
    }

    public ConversationResponse toDto(Conversation conversation) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .buyerId(conversation.getBuyer().getBuyerId())
                .postId(conversation.getPostProduct().getId())
                .createdAt(conversation.getCreatedAt())
                .build();
    }
}
