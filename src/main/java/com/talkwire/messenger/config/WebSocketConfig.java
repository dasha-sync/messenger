package com.talkwire.messenger.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@AllArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app"); // для входящих
    registry.enableSimpleBroker("/topic", "/queue");    // для исходящих
    registry.setUserDestinationPrefix("/user");         // для приватных сообщений
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
        .setAllowedOrigins("http://localhost:63342") // TODO: поменять на фронтовый домен
        .addInterceptors(jwtHandshakeInterceptor)
        .withSockJS();
  }
}
