package com.talk_wire.messenger.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${messenger.app.secret}")
    private String secret;
    @Value("${messenger.app.expiration}")
    private long expiration;

    // TODO: Обновить jjwt до 12й версии
    public String generateToken(Authentication authentication) throws NoSuchAlgorithmException {
        UserDetailsImplementation userDetails = (UserDetailsImplementation) authentication.getPrincipal();
       /* SecureRandom secureRandom = SecureRandom.getInstance(secret);
        Key key = Jwts.SIG.HS256.key().random(secureRandom).build();*/

        return Jwts.builder().setSubject(userDetails.getUsername()).setIssuedAt((new Date()))
                .setExpiration(new Date((new Date()).getTime() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
                /*.issuedAt(new Date())
                .subject(userDetails.getUsername())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();*/
    }

    public String getNameFromJwt(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }
}
