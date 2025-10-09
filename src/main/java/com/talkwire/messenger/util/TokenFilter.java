package com.talkwire.messenger.util;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TokenFilter extends OncePerRequestFilter {
  private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  private final Map<String, CachedAuth> tokenCache = new ConcurrentHashMap<>();

  public TokenFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userDetailsService = userDetailsService;
  }

  @Getter
  private static class CachedAuth {
    private final UsernamePasswordAuthenticationToken auth;
    private final long creationTime = System.currentTimeMillis();

    public CachedAuth(UsernamePasswordAuthenticationToken auth) {
      this.auth = auth;
    }
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    cleanupExpiredCache();

    String jwt = extractJwtFromRequest(request);

    if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      authenticateUsingToken(jwt);
    }

    filterChain.doFilter(request, response);
  }

  private String extractJwtFromRequest(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null)
      return null;

    return Arrays.stream(cookies)
        .filter(cookie -> "jwt".equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }

  private void authenticateUsingToken(String jwt) {
    CachedAuth cached = tokenCache.get(jwt);
    if (cached != null) {
      SecurityContextHolder.getContext().setAuthentication(cached.getAuth());
      return;
    }

    try {
      String username = jwtTokenProvider.getNameFromJwt(jwt);
      if (username == null)
        return;

      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null,
          userDetails.getAuthorities());

      tokenCache.put(jwt, new CachedAuth(auth));
      SecurityContextHolder.getContext().setAuthentication(auth);

    } catch (ExpiredJwtException e) {
      log.warn("JWT expired", e);
      tokenCache.remove(jwt);
    } catch (Exception e) {
      log.error("JWT authentication error", e);
      tokenCache.remove(jwt);
    }
  }

  private void cleanupExpiredCache() {
    long now = System.currentTimeMillis();
    tokenCache.entrySet().removeIf(entry -> now - entry.getValue().getCreationTime() > CACHE_TTL_MS);
  }
}
