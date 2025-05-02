package com.talkwire.messenger.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Handles JWT generation and parsing.
 */
@Component
public class JwtTokenProvider {

  @Value("${messenger.app.secret}")
  private String secret;

  @Value("${messenger.app.expiration}")
  private long expiration;

  /**
   * Generates a JWT for the authenticated user.
   *
   * @param auth the current authentication context
   * @return signed JWT string
   * @throws NoSuchAlgorithmException if key generation fails (not used currently)
   */
  public String generateToken(Authentication auth) throws NoSuchAlgorithmException {
    UserDetailsImplementation userDetails = (UserDetailsImplementation) auth.getPrincipal();

    return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact();
  }

  /**
   * Extracts the username from a JWT.
   *
   * @param token the JWT string
   * @return username stored in the token
   */
  public String getNameFromJwt(String token) {
    return Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
  }
}
