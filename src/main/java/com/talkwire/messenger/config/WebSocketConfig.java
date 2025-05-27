package com.talkwire.messenger.config;

import com.talkwire.messenger.util.CustomHandshakeHandler;
import com.talkwire.messenger.util.JwtHandshakeInterceptor;
import com.talkwire.messenger.util.WebSocketSecurityInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@AllArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
  private final WebSocketSecurityInterceptor webSocketSecurityInterceptor;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app"); // для входящих
    registry.enableSimpleBroker("/topic", "/queue"); // для исходящих
    registry.setUserDestinationPrefix("/user"); // для приватных сообщений
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
        .addInterceptors(jwtHandshakeInterceptor)
        .setHandshakeHandler(new CustomHandshakeHandler())
        .setAllowedOrigins("http://localhost:5173", "http://localhost:5174")
        .withSockJS()
        .setSessionCookieNeeded(true)
        .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(webSocketSecurityInterceptor);
  }

  @Override
  public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
    registration.setMessageSizeLimit(8192) // 8KB
        .setSendBufferSizeLimit(512 * 1024) // 512KB
        .setSendTimeLimit(20000); // 20 seconds
  }
}
