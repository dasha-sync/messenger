package com.talkwire.messenger.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that checks for a JWT in the Authorization header and
 * sets authentication in the context if valid.
 */
@Component
public class TokenFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  /**
   * Constructs a TokenFilter with required dependencies.
   *
   * @param jwtTokenProvider     handles token parsing
   * @param userDetailsService   loads user details by username
   */
  public TokenFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userDetailsService = userDetailsService;
  }

  /**
   * Intercepts each request, extracts and validates JWT, and
   * sets authentication in context if valid.
   */
  @Override
  protected void doFilterInternal(
          @NonNull HttpServletRequest request,
          @NonNull HttpServletResponse response,
          @NonNull FilterChain filterChain)
          throws ServletException, IOException {

    String jwt = null;
    String username = null;
    UserDetails userDetails = null;
    UsernamePasswordAuthenticationToken auth = null;

    try {
      String headerAuth = request.getHeader("Authorization");
      if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
        jwt = headerAuth.substring(7);
      }

      if (jwt != null) {
        try {
          username = jwtTokenProvider.getNameFromJwt(jwt);
        } catch (ExpiredJwtException e) {
          logger.error("Token expired: ", e);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
          userDetails = userDetailsService.loadUserByUsername(username);
          auth = new UsernamePasswordAuthenticationToken(
                  userDetails,
                  null,
                  userDetails.getAuthorities()
          );
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      }

    } catch (Exception e) {
      logger.error("Token filter error: ", e);
    }

    filterChain.doFilter(request, response);
  }
}
