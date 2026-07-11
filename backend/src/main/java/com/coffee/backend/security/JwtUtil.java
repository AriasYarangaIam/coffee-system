package com.coffee.backend.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilidad para crear y leer los JSON Web Tokens (JWT) de la sesión. El token guarda el
 * correo (subject) y el rol, va firmado con una clave secreta (HMAC-SHA) y tiene fecha de
 * expiración. El secreto y la duración se leen de configuración ({@code jwt.secret},
 * {@code jwt.expiration}).
 */
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secreto;

    @Value("${jwt.expiration}")
    private long expiracion;

    // Deriva la clave de firma a partir del secreto configurado.
    private SecretKey obtenerClaveSecreta() {
        return Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    /** Crea un token firmado con el correo como subject y el rol como claim. */
    public String generarToken(String correo, String rol) {
        return Jwts.builder()
                .subject(correo)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiracion))
                .signWith(obtenerClaveSecreta())
                .compact();
    }

    /** Verifica la firma y devuelve el contenido (claims) del token. Lanza si es inválido. */
    public Claims extraerTodosLosClaims(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClaveSecreta())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Correo (subject) guardado en el token. */
    public String extraerCorreo(String token) {
        return extraerTodosLosClaims(token).getSubject();
    }

    /** Rol (claim "rol") guardado en el token. */
    public String extraerRol(String token) {
        return extraerTodosLosClaims(token).get("rol", String.class);
    }

    /** True si el token es válido (firma correcta y no expirado); false ante cualquier error. */
    public boolean esTokenValido(String token) {
        try {
            return extraerTodosLosClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
