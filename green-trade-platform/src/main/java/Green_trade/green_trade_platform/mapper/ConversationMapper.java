package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Conversation;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.response.ConversationResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConversationMapper {

    public Conversation toEntity(Buyer buyer, PostProduct postProduct) {
        return Conversation.builder()
                .buyer(buyer)
                .postProduct(postProduct)
                .build();
    }

    public ConversationResponse toDto(Conversation conversation) {
        Buyer buyer = conversation.getBuyer();
        PostProduct postProduct = conversation.getPostProduct();
        Buyer sellerBuyer = postProduct.getSeller() != null ? postProduct.getSeller().getBuyer() : null;

        return ConversationResponse.builder()
                .id(conversation.getId())
                .buyerId(buyer.getBuyerId())
                .buyerName(buyer.getFullName() != null ? buyer.getFullName() : buyer.getUsername())
                .buyerAvatar(buyer.getAvatarUrl())
                .sellerId(postProduct.getSeller() != null ? postProduct.getSeller().getSellerId() : null)
                .sellerName(postProduct.getSeller() != null ? postProduct.getSeller().getSellerName() : null)
                .sellerStoreName(postProduct.getSeller() != null ? postProduct.getSeller().getStoreName() : null)
                .sellerAvatar(sellerBuyer != null ? sellerBuyer.getAvatarUrl() : null)
                .postId(postProduct.getId())
                .createdAt(conversation.getCreatedAt())
                .build();
    }

    public List<ConversationResponse> toDtoList(List<Conversation> conversations) {
        return conversations.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

}
