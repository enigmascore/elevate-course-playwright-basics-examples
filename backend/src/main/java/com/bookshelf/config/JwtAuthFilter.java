package com.bookshelf.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bookshelf.auth.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Reads "Authorization: Bearer &lt;jwt&gt;" and, if valid, authenticates the request. */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter( JwtService jwtService ) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response,
            FilterChain chain ) throws ServletException, IOException {

        String header = request.getHeader( "Authorization" );

        if ( header != null && header.startsWith( "Bearer " ) ) {
            jwtService.subjectOf( header.substring( 7 ) ).ifPresent( email ->
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken( email, null, List.of() ) ) );
        }

        chain.doFilter( request, response );
    }
}
