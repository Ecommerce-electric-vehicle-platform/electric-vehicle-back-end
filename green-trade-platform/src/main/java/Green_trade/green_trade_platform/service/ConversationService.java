package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Conversation;

import java.util.List;

public interface ConversationService {
    Conversation createConversation(Conversation conversation);

    List<Conversation> getConversation(Buyer buyer);

    Conversation findById(Long conversationId);
}

