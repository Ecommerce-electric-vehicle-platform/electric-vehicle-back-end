package Green_trade.green_trade_platform.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chatting")
@AllArgsConstructor
public class ChattingController {
    private NotificationSocketController socketController;

//    @Operation(
//
//    )
//    @PostMapping("/create-conversation")
//    public ResponseEntity<?> createConversation()
}
