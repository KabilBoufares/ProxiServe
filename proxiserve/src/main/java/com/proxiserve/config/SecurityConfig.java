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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;


/**
 * Configuration de la sécurité Spring Boot
 */
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Constructeur avec injection de dépendances
     * @param jwtTokenProvider fournisseur de tokens JWT
     * @param userDetailsService service de gestion des utilisateurs
     */
    public SecurityConfig(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configuration de la sécurité Spring Boot
     * @param http configuration de sécurité HTTP
     * @return SecurityFilterChain
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Routes accessibles sans authentification
                .requestMatchers(HttpMethod.POST, "/api/auth/signup", "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/services/artisan/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_CLIENT", "ROLE_ARTISAN")

                
                .requestMatchers(HttpMethod.GET, "/api/artisans/nearby").hasAuthority("ROLE_CLIENT") // Corrigé
                .requestMatchers(HttpMethod.GET, "/api/services/search/advanced").permitAll()
                // Artisans peuvent confirmer une réservation
                .requestMatchers(HttpMethod.PUT, "/api/bookings/**/confirm").hasAuthority("ROLE_ARTISAN")


                .requestMatchers(HttpMethod.POST, "/api/bookings").hasAuthority("ROLE_CLIENT")
                .requestMatchers(HttpMethod.DELETE, "/api/bookings/**").hasAuthority("ROLE_CLIENT")

                .requestMatchers(HttpMethod.POST, "/api/services").hasAuthority("ROLE_ARTISAN")
                // Admins peuvent voir le dashboard
                .requestMatchers(HttpMethod.GET, "/api/admin/dashboard").hasAuthority("ROLE_ADMIN")
                //  Admins peuvent voir les clients
                .requestMatchers(HttpMethod.GET, "/api/clients").hasAuthority("ROLE_ADMIN") 
                //  Admins peuvent voir les artisans
                .requestMatchers(HttpMethod.GET, "/api/artisans").hasAuthority("ROLE_ADMIN")
                
    
                // Clients et Artisans peuvent voir les artisans
                .requestMatchers(HttpMethod.GET, "/api/artisans").hasAnyAuthority("ROLE_ADMIN", "ROLE_CLIENT", "ROLE_ARTISAN")
                

                // Toute autre requête nécessite une authentification
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService), 
                            UsernamePasswordAuthenticationFilter.class);
    
        return http.build();
    }


    
    

    /**
     * Bean pour le hachage des mots de passe avec BCrypt
     * @return PasswordEncoder instance de BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean pour la gestion de l'authentification
     * @param authConfig configuration d'authentification
     * @return AuthenticationManager
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configuration du serveur embarqué Tomcat pour la redirection HTTP → HTTPS
     * @return ServletWebServerFactory
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(httpToHttpsRedirectConnector());
        return tomcat;
    }

    /**
     * Configuration du connecteur pour la redirection automatique HTTP vers HTTPS
     * @return Connector configuré pour redirection
     */
    private Connector httpToHttpsRedirectConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080); // Port HTTP utilisé
        connector.setSecure(false);
        connector.setRedirectPort(8443); // Redirection automatique vers HTTPS (port sécurisé)
        return connector;
    }
}
