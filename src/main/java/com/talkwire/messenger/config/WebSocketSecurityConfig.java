package com.talkwire.messenger.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.*;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.messaging.access.intercept.*;

@Configuration
@Slf4j
public class WebSocketSecurityConfig {

  @Bean
  public AuthorizationManager<Message<?>> messageAuthorizationManager() {
    MessageMatcherDelegatingAuthorizationManager
        .Builder messages = MessageMatcherDelegatingAuthorizationManager
        .builder();

    messages
        .simpTypeMatchers(
            SimpMessageType.CONNECT,
            SimpMessageType.HEARTBEAT,
            SimpMessageType.UNSUBSCRIBE,
            SimpMessageType.DISCONNECT,
            SimpMessageType.SUBSCRIBE,
            SimpMessageType.MESSAGE)
        .permitAll()
        .simpDestMatchers("/app/**").permitAll()
        .simpSubscribeDestMatchers("/topic/**", "/queue/**").permitAll()
        .anyMessage().permitAll();

    return messages.build();
  }

  @Bean
  public ChannelInterceptor securityInterceptor(
      AuthorizationManager<Message<?>> authorizationManager) {
    return new ChannelInterceptor() {
      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
          log.debug("Current authentication in security interceptor: {}", auth);
        } else {
          log.warn("No authentication in security interceptor");
        }
        return message;
      }
    };
  }
}
