package br.com.fiap.fordradar.security;

import br.com.fiap.fordradar.models.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expirationTimeMillis;

    @Value("${jwt.issuer:ford-radar-api}")
    private String issuer;

    @PostConstruct
    void validateConfiguration() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET ausente ou com menos de 32 bytes. Gere com: openssl rand -base64 48");
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant validity = now.plus(expirationTimeMillis, ChronoUnit.MILLIS);

        return Jwts.builder()
                .issuer(issuer)
                .id(UUID.randomUUID().toString())
                .subject(user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(validity))
                .signWith(getSigningKey())
                .compact();
    }

    public String validateToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
