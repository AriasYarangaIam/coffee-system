package com.coffee.backend.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que se ejecuta una vez por petición (extends {@link OncePerRequestFilter}). Lee el
 * header {@code Authorization: Bearer <token>}; si el JWT es válido, carga el usuario y lo
 * deja autenticado en el contexto de seguridad para que Spring aplique los permisos por rol.
 * Si no hay token o es inválido, deja pasar sin autenticar (y la autorización lo cortará).
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter{
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest solicitud,
                                    HttpServletResponse respuesta,
                                    FilterChain cadenaFiltros)
            throws ServletException, IOException {
        String encabezadoAuth = solicitud.getHeader("Authorization");

        // Sin header Bearer: no hay nada que autenticar, sigue la cadena.
        if (encabezadoAuth == null || !encabezadoAuth.startsWith("Bearer ")) {
            cadenaFiltros.doFilter(solicitud, respuesta);
            return;
        }

        String token = encabezadoAuth.substring(7); // quita el prefijo "Bearer "

        // Token inválido o expirado: sigue sin autenticar.
        if (!jwtUtil.esTokenValido(token)) {
            cadenaFiltros.doFilter(solicitud, respuesta);
            return;
        }

        String correo = jwtUtil.extraerCorreo(token);

        // Con un correo válido y sin autenticación previa, carga el usuario y lo marca autenticado.
        if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails detallesUsuario = userDetailsServiceImpl.loadUserByUsername(correo);

            UsernamePasswordAuthenticationToken tokenAutenticacion =
                    new UsernamePasswordAuthenticationToken(
                            detallesUsuario, null, detallesUsuario.getAuthorities());

            tokenAutenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(solicitud));
            SecurityContextHolder.getContext().setAuthentication(tokenAutenticacion);
        }

        cadenaFiltros.doFilter(solicitud, respuesta);
    }
}
