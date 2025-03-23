package com.proxiserve.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final String signingKey = "a7rz/lF39+P8ZRrljTiEX7fkY0qGycCUocGXrax+qKv1JRqhGbzTO7yMqUoP5DAH4+txc0XIPcJDfQ3qf7/s7g=="; // Replace with your actual signing key

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = getTokenFromRequest(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getUserEmailFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getUserEmailFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        
            System.out.println(" [DEBUG] Utilisateur authentifié : " + email);
            System.out.println(" [DEBUG] Rôles récupérés : " + userDetails.getAuthorities());
        
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


public Authentication getAuthentication(String token) {
    
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();

    String email = claims.getSubject();
    String role = claims.get("role", String.class);

    System.out.println(" [DEBUG] Token Décrypté → Email: " + email + " | Role: " + role); //  Debug Token

    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
}





}
