package com.proxiserve.config;

import com.proxiserve.security.CustomUserDetailsService;
import com.proxiserve.security.jwt.JwtAuthenticationFilter;
import com.proxiserve.security.jwt.JwtTokenProvider;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // Désactiver CSRF pour les API stateless
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT = stateless
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/request-reset-password" , "/api/auth/reset-password").permitAll() // Routes publiques
            .requestMatchers(HttpMethod.GET, "/ping", "/test-db").permitAll() // Routes publiques
            .requestMatchers("/api/artisans/**").hasAuthority("ROLE_ARTISAN") // Artisan-specific routes
            .requestMatchers("/api/auth/validate-token").authenticated() // ✅ autorisé tout les auth users
            .anyRequest().authenticated() // Toute autre route nécessite une authentification
        )
        .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService), 
                         UsernamePasswordAuthenticationFilter.class); // Filtre JWT

    return http.build();
}


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Hashage des mots de passe
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     *  Redirection automatique HTTP → HTTPS
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(httpToHttpsRedirectConnector());
        return tomcat;
    }

    private Connector httpToHttpsRedirectConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080); // Port HTTP
        connector.setSecure(false);
        connector.setRedirectPort(8443); // Redirige automatiquement vers HTTPS
        return connector;
    }
}
