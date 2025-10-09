package com.talkwire.messenger.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null) {
      return message;
    }

    String token = extractToken(accessor);

    if (!StringUtils.hasText(token)) {
      log.warn("No token found in WebSocket headers");
      return message;
    }

    authenticateUser(token, accessor);
    return message;
  }

  private void authenticateUser(String token, StompHeaderAccessor accessor) {
    try {
      String username = jwtTokenProvider.getNameFromJwt(token);
      if (!StringUtils.hasText(username)) {
        log.warn("Token parsing failed: username is null or empty");
        return;
      }

      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      SecurityContextHolder.getContext().setAuthentication(authentication);
      accessor.setUser(authentication);

      log.info("WebSocket user authenticated: {}", username);

    } catch (Exception e) {
      log.error("WebSocket authentication failed", e);
    }
  }

  private String extractToken(StompHeaderAccessor accessor) {
    String cookieHeader = accessor.getFirstNativeHeader("Cookie");
    if (StringUtils.hasText(cookieHeader)) {
      for (String cookie : cookieHeader.split(";")) {
        cookie = cookie.trim();
        if (cookie.startsWith("jwt=")) {
          return cookie.substring(4);
        }
      }
    }

    String authHeader = accessor.getFirstNativeHeader("Authorization");
    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    String tokenParam = accessor.getFirstNativeHeader("token");
    return StringUtils.hasText(tokenParam) ? tokenParam : null;
  }
}
