package com.talkwire.messenger.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TokenFilter extends OncePerRequestFilter {
  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  // Create a wrapper class to store auth token and creation time
  private static class CachedAuth {
    private final UsernamePasswordAuthenticationToken auth;
    private final long creationTime;

    public CachedAuth(UsernamePasswordAuthenticationToken auth) {
      this.auth = auth;
      this.creationTime = System.currentTimeMillis();
    }

    public UsernamePasswordAuthenticationToken getAuth() {
      return auth;
    }

    public long getCreationTime() {
      return creationTime;
    }
  }

  // Update the cache to use the wrapper
  private final ConcurrentHashMap<String, CachedAuth> tokenCache = new ConcurrentHashMap<>();
  private static final long CACHE_TTL = 5 * 60 * 1000; // 5 minutes

  public TokenFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(
          @NonNull HttpServletRequest request,
          @NonNull HttpServletResponse response,
          @NonNull FilterChain filterChain)
          throws ServletException, IOException {

    String jwt = null;
    String headerAuth = request.getHeader("Authorization");

    if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
      jwt = headerAuth.substring(7);
    }

    if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      CachedAuth cachedAuth = tokenCache.get(jwt);
      // Check if the token is in the cache
      if (cachedAuth != null) {
        SecurityContextHolder.getContext().setAuthentication(cachedAuth.getAuth());
      } else {
        try {
          String username = jwtTokenProvider.getNameFromJwt(jwt);
          if (username != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            // Save to cache with wrapper
            tokenCache.put(jwt, new CachedAuth(auth));
            SecurityContextHolder.getContext().setAuthentication(auth);
          }
        } catch (ExpiredJwtException e) {
          logger.error("Token expired: ", e);
          tokenCache.remove(jwt);
        } catch (Exception e) {
          logger.error("Token filter error: ", e);
          tokenCache.remove(jwt);
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  private void cleanupCache() {
    long currentTime = System.currentTimeMillis();
    tokenCache.entrySet().removeIf(entry ->
        currentTime - entry.getValue().getCreationTime() > CACHE_TTL);
  }
}
