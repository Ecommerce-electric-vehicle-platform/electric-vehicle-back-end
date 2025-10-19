package Green_trade.green_trade_platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // endpoint WebSocket
                .setAllowedOriginPatterns("*") // cho phép frontend truy cập
                .withSockJS(); // fallback cho browser cũ
//        registry.addEndpoint("/ws")
//                .setAllowedOriginPatterns("*"); // raw WebSocket endpoint
//        registry.addEndpoint("/ws-sockjs")
//                .setAllowedOriginPatterns("*")
//                .withSockJS(); // SockJS fallback
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /app là prefix khi client gửi lên server
        registry.setApplicationDestinationPrefixes("/app");

        // /topic và /queue là prefix server đẩy dữ liệu xuống client
        registry.enableSimpleBroker("/topic", "/queue");
    }
}
