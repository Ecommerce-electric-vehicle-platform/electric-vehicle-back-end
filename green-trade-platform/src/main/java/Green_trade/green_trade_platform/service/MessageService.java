package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.Conversation;
import Green_trade.green_trade_platform.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MessageService {
    Message handleImageMessage(Message message, MultipartFile picture) throws IOException;

    Message handleTextmessage(Message message);

    Page<Message> getConversationMessages(int page, int size, Conversation conversation);
}

