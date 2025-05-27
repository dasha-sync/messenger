package com.talkwire.messenger.util;

import com.talkwire.messenger.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor
        .getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null) {
      log.info("Processing message type: {}", accessor.getCommand());
      log.info("Message headers: {}", accessor.toNativeHeaderMap());

      String authHeader = accessor.getFirstNativeHeader("Authorization");
      log.info("Authorization header: {}", authHeader);

      String token = extractToken(accessor);
      log.info("Extracted token: {}", token != null ? "present" : "null");

      if (token != null) {
        try {
          String username = jwtTokenProvider.getNameFromJwt(token);
          log.info("Username from token: {}", username);

          if (username != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            accessor.setUser(auth);
            log.info("WebSocket user authenticated: {}", username);
          }
        } catch (Exception e) {
          log.error("WebSocket authentication failed: ", e);
        }
      } else {
        log.warn("No token found in message headers");
      }
    }

    return message;
  }

  private String extractToken(StompHeaderAccessor accessor) {
    // Проверяем куки в первую очередь
    String cookieHeader = accessor.getFirstNativeHeader("Cookie");
    if (StringUtils.hasText(cookieHeader)) {
      String[] cookies = cookieHeader.split(";");
      for (String cookie : cookies) {
        if (cookie.trim().startsWith("jwt=")) {
          return cookie.trim().substring(4);
        }
      }
    }

    // Для обратной совместимости проверяем другие способы
    String authHeader = accessor.getFirstNativeHeader("Authorization");
    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    String query = accessor.getFirstNativeHeader("token");
    if (StringUtils.hasText(query)) {
      return query;
    }

    return null;
  }
}
