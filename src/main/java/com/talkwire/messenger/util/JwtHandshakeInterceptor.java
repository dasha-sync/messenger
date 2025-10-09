package com.talkwire.messenger.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
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

    logRequestDetails(request);

    String token = extractJwtToken(request);
    if (token == null) {
      log.warn("No valid JWT token found in request");
      return false;
    }

    return authenticateFromToken(token, attributes);
  }

  @Override
  public void afterHandshake(
      @NonNull ServerHttpRequest request,
      @NonNull ServerHttpResponse response,
      @NonNull WebSocketHandler wsHandler,
      @Nullable Exception exception) {
    // No-op
  }

  private boolean authenticateFromToken(String token, Map<String, Object> attributes) {
    try {
      String username = jwtTokenProvider.getNameFromJwt(token);
      if (username == null) {
        log.warn("Username could not be extracted from token");
        return false;
      }

      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      Authentication auth = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());

      SecurityContextHolder.getContext().setAuthentication(auth);
      attributes.put("auth", auth);

      log.info("Authentication successful for user: {}", username);
      return true;

    } catch (Exception e) {
      log.error("Authentication failed: ", e);
      return false;
    }
  }

  private String extractJwtToken(ServerHttpRequest request) {
    if (request instanceof ServletServerHttpRequest servletRequest) {
      HttpServletRequest httpRequest = servletRequest.getServletRequest();

      String tokenFromCookie = extractFromCookies(httpRequest.getCookies());
      if (tokenFromCookie != null) return tokenFromCookie;
    }

    String tokenFromHeader = extractFromAuthorizationHeader(request);
    if (tokenFromHeader != null) return tokenFromHeader;

    return extractFromQueryParams(request);
  }

  private String extractFromCookies(Cookie[] cookies) {
    if (cookies == null) return null;

    return Arrays.stream(cookies)
        .filter(cookie -> "jwt".equals(cookie.getName()))
        .peek(cookie -> log.info("JWT token found in cookie"))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }

  private String extractFromAuthorizationHeader(ServerHttpRequest request) {
    List<String> authHeaders = request.getHeaders().get("Authorization");

    if (authHeaders != null && !authHeaders.isEmpty()) {
      String header = authHeaders.get(0);
      if (header.startsWith("Bearer ")) {
        log.info("JWT token found in Authorization header");
        return header.substring(7);
      }
    }

    return null;
  }

  private String extractFromQueryParams(ServerHttpRequest request) {
    Map<String, String> queryParams = UriComponentsBuilder
        .fromUri(request.getURI())
        .build()
        .getQueryParams()
        .toSingleValueMap();

    if (queryParams.containsKey("token")) {
      log.info("JWT token found in query parameter");
      return queryParams.get("token");
    }

    return null;
  }

  private void logRequestDetails(ServerHttpRequest request) {
    log.info("WebSocket handshake initiated");
    log.info("Request URI: {}", request.getURI());
    log.info("Request headers: {}", request.getHeaders());
    log.info("Current authentication: {}", SecurityContextHolder.getContext().getAuthentication());

    if (request instanceof ServletServerHttpRequest servletRequest) {
      HttpServletRequest httpRequest = servletRequest.getServletRequest();
      log.info("Cookies: {}", Arrays.toString(httpRequest.getCookies()));
    }
  }
}
