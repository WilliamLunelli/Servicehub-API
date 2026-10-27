package com.servicehub.api.security;

import com.servicehub.api.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /** Gera um token assinado (HS256) com sub = e-mail, role e exp (apenas estes tres claims). Nao inclui senha nem dados sensiveis. */
    public String generateToken(String email, Role role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(email)
                .claim("role", role.name())
                .expiration(new Date(now + expirationMs))
                .signWith(key)
                .compact();
    }

    /** Valida assinatura e expiracao. Qualquer token adulterado, expirado ou malformado retorna false. */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
