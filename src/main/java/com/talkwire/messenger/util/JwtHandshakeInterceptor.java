package com.talkwire.messenger.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.*;
import org.springframework.lang.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  @Override
  public boolean beforeHandshake(
      @NonNull ServerHttpRequest request,
      @NonNull ServerHttpResponse response,
      @NonNull WebSocketHandler wsHandler,
      @NonNull Map<String, Object> attributes) {

    log.info("WebSocket handshake request received");
    log.info("Request URI: {}", request.getURI());
    log.info("Request headers: {}", request.getHeaders());
    log.info("Current authentication: {}", SecurityContextHolder.getContext().getAuthentication());

    if (request instanceof ServletServerHttpRequest servletRequest) {
      HttpServletRequest httpRequest = servletRequest.getServletRequest();
      log.info("Cookies: {}", Arrays.toString(httpRequest.getCookies()));
    }

    String token = getJwtFromRequest(request);
    log.info("Token found: {}", token != null);

    if (token != null) {
      try {
        String username = jwtTokenProvider.getNameFromJwt(token);
        log.info("Username from token: {}", username);

        if (username != null) {
          UserDetails userDetails = userDetailsService.loadUserByUsername(username);
          Authentication auth = new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities());
          SecurityContextHolder.getContext().setAuthentication(auth);
          attributes.put("auth", auth);
          log.info("Authentication successful for user: {}", username);
          return true;
        }
      } catch (Exception e) {
        log.error("Authentication failed: ", e);
      }
    }

    log.warn("No valid token found in request");
    return false;
  }

  @Override
  public void afterHandshake(
      @NonNull ServerHttpRequest request,
      @NonNull ServerHttpResponse response,
      @NonNull WebSocketHandler wsHandler,
      @Nullable Exception exception) {

  }

  private String getJwtFromRequest(ServerHttpRequest request) {
    // Проверяем куки в первую очередь
    if (request instanceof ServletServerHttpRequest servletRequest) {
      HttpServletRequest httpRequest = servletRequest.getServletRequest();
      Cookie[] cookies = httpRequest.getCookies();
      if (cookies != null) {
        for (Cookie cookie : cookies) {
          if ("jwt".equals(cookie.getName())) {
            System.out.println("Found JWT cookie");
            return cookie.getValue();
          }
        }
      }
    }

    List<String> authHeaders = request.getHeaders().get("Authorization");
    if (authHeaders != null && !authHeaders.isEmpty()) {
      String header = authHeaders.get(0);
      if (header.startsWith("Bearer ")) {
        return header.substring(7);
      }
    }

    String query = request.getURI().getQuery();
    if (query != null) {
      Map<String, String> queryParams = UriComponentsBuilder
          .fromUri(request.getURI())
          .build()
          .getQueryParams()
          .toSingleValueMap();

      if (queryParams.containsKey("token")) {
        return queryParams.get("token");
      }
    }

    return null;
  }
}
