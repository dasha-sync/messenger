package com.talkwire.messenger.util;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  @Override
  public boolean beforeHandshake(
      @NonNull ServerHttpRequest request,
      @NonNull ServerHttpResponse response,
      @NonNull WebSocketHandler wsHandler,
      @NonNull Map<String, Object> attributes) {
    String token = getJwtFromRequest(request);
    if (token != null) {
      String username = jwtTokenProvider.getNameFromJwt(token);
      if (username != null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication auth = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    }
    return true;
  }

  @Override
  public void afterHandshake(
      @NonNull ServerHttpRequest request,
      @NonNull ServerHttpResponse response,
      @NonNull WebSocketHandler wsHandler,
      @Nullable Exception exception) {

  }

  private String getJwtFromRequest(ServerHttpRequest request) {
    List<String> authHeaders = request.getHeaders().get("Authorization");
    if (authHeaders != null && !authHeaders.isEmpty()) {
      String header = authHeaders.get(0);
      if (header.startsWith("Bearer ")) {
        return header.substring(7);
      }
    }

    // fallback to query param for SockJS compatibility
    String query = request.getURI().getQuery();
    if (query != null) {
      Map<String, String> queryParams = UriComponentsBuilder
          .fromUri(request.getURI())
          .build()
          .getQueryParams()
          .toSingleValueMap();

      return queryParams.get("token");
    }

    return null;
  }
}
