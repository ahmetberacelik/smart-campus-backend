package com.smartcampus.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration for real-time notifications.
 * 
 * Client connection: ws://localhost:8081/ws
 * Subscribe to: /user/queue/notifications
 * 
 * Frontend (SockJS + STOMP.js):
 * const socket = new SockJS('http://localhost:8081/ws');
 * const stompClient = Stomp.over(socket);
 * stompClient.connect({Authorization: 'Bearer <token>'}, () => {
 * stompClient.subscribe('/user/queue/notifications', (message) => {
 * console.log(JSON.parse(message.body));
 * });
 * });
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /queue için simple broker kullan (notifications, messages vb.)
        config.enableSimpleBroker("/queue", "/topic");
        // Client'tan gelen mesajlar için prefix
        config.setApplicationDestinationPrefixes("/app");
        // User-specific destination prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint - SockJS fallback ile
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Pure WebSocket endpoint (SockJS olmadan)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}
