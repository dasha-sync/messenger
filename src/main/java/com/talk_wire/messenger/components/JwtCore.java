package com.talk_wire.messenger.components;

import com.talk_wire.messenger.models.UserDetailsImplementation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import java.net.Authenticator;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;


@Component
public class JwtCore {
    @Value("${messenger.app.secret}")
    private String secret;
    @Value("${messenger.app.expiration}")
    private int expiration;

    public String generateToken(Authentication authentication) throws NoSuchAlgorithmException {
        UserDetailsImplementation userDetails = (UserDetailsImplementation) authentication.getPrincipal();
        SecureRandom secureRandom = SecureRandom.getInstance(secret);
        Key key = Jwts.SIG.HS256.key().random(secureRandom).build();

        return Jwts.builder()
                .issuedAt(new Date())
                .subject(userDetails.getUsername())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }
}
