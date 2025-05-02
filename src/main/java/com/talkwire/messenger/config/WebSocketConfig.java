package com.talkwire.messenger.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures WebSocket messaging using STOMP over SockJS.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  /**
   * Sets up the message broker with a simple in-memory topic and application prefix.
   *
   * @param config broker registry to be configured
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app");
  }

  /**
   * Registers the STOMP endpoint for client connections, with SockJS fallback support.
   *
   * @param registry STOMP endpoint registry
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
            .setAllowedOrigins("http://localhost:63342")
            .withSockJS();
  }
}
