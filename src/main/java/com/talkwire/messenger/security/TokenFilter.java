package com.talkwire.messenger.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class TokenFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  private static final long CACHE_TTL = 5 * 60 * 1000; // 5 minutes
  private final ConcurrentHashMap<String, CachedAuth> tokenCache = new ConcurrentHashMap<>();

  public TokenFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userDetailsService = userDetailsService;
  }

  @Getter
  private static class CachedAuth {
    private final UsernamePasswordAuthenticationToken auth;
    private final long creationTime;

    public CachedAuth(UsernamePasswordAuthenticationToken auth) {
      this.auth = auth;
      this.creationTime = System.currentTimeMillis();
    }
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {

    cleanupCache();

    String jwt = extractJwtFromRequest(request);

    if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      CachedAuth cachedAuth = tokenCache.get(jwt);

      if (cachedAuth != null) {
        SecurityContextHolder.getContext().setAuthentication(cachedAuth.getAuth());
      } else {
        authenticateAndCache(jwt);
      }
    }

    filterChain.doFilter(request, response);
  }

  private String extractJwtFromRequest(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");
    return (headerAuth != null && headerAuth.startsWith("Bearer "))
        ? headerAuth.substring(7)
        : null;
  }

  private void authenticateAndCache(String jwt) {
    try {
      String username = jwtTokenProvider.getNameFromJwt(jwt);
      if (username != null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());

        tokenCache.put(jwt, new CachedAuth(auth));
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    } catch (ExpiredJwtException e) {
      log.warn("JWT expired", e);
      tokenCache.remove(jwt);
    } catch (Exception e) {
      log.error("Error while authenticating JWT", e);
      tokenCache.remove(jwt);
    }
  }

  private void cleanupCache() {
    long currentTime = System.currentTimeMillis();
    tokenCache.entrySet().removeIf(entry ->
        currentTime - entry.getValue().getCreationTime() > CACHE_TTL);
  }
}
