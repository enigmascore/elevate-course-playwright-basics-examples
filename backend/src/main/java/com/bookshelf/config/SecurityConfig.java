package com.bookshelf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security. Everything under /api needs a Bearer token except:
 * the health probe ( the Playwright fixture polls it before any login ), the
 * auth endpoints, and book covers ( an &lt;img src&gt; cannot send a header ).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain( HttpSecurity http, JwtAuthFilter jwtAuthFilter )
            throws Exception {

        http.csrf( csrf -> csrf.disable() )
                .sessionManagement( s -> s.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) )
                .authorizeHttpRequests( auth -> auth
                        .requestMatchers( "/api/health", "/api/auth/**" ).permitAll()
                        .requestMatchers( HttpMethod.GET, "/api/books/*/cover" ).permitAll()
                        .anyRequest().authenticated() )
                .exceptionHandling( e -> e.authenticationEntryPoint(
                        ( request, response, ex ) -> response.sendError( 401 ) ) )
                .addFilterBefore( jwtAuthFilter, UsernamePasswordAuthenticationFilter.class );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
