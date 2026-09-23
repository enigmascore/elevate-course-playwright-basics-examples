package com.bookshelf.auth;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Issues and verifies the JWT the front end keeps in localStorage. The secret is
 * FIXED in application.yml on purpose: a token saved by one backend ( the
 * storage-state lesson ) must still verify against the next fresh one.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long ttlMs;

    public JwtService( @Value( "${app.jwt.secret}" ) String secret,
            @Value( "${app.jwt.ttl-ms}" ) long ttlMs ) {
        this.key = Keys.hmacShaKeyFor( secret.getBytes( StandardCharsets.UTF_8 ) );
        this.ttlMs = ttlMs;
    }

    public String issue( String email ) {
        Date now = new Date();
        return Jwts.builder()
                .subject( email )
                .issuedAt( now )
                .expiration( new Date( now.getTime() + ttlMs ) )
                .signWith( key )
                .compact();
    }

    /** The email inside a valid, unexpired token; empty for anything else. */
    public Optional<String> subjectOf( String token ) {
        try {
            return Optional.of( Jwts.parser().verifyWith( key ).build()
                    .parseSignedClaims( token ).getPayload().getSubject() );
        }
        catch ( JwtException | IllegalArgumentException e ) {
            return Optional.empty();
        }
    }
}
