package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Conversation;
import Green_trade.green_trade_platform.repository.ConversationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ConversationServiceImpl {
    private final ConversationRepository conversationRepository;

    public Conversation createConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }
}
