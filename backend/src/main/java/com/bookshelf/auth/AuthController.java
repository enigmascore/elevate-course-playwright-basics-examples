package com.bookshelf.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bookshelf.auth.dto.ForgotPasswordRequest;
import com.bookshelf.auth.dto.LoginRequest;
import com.bookshelf.auth.dto.LoginResponse;
import com.bookshelf.auth.dto.RegisterRequest;
import com.bookshelf.auth.dto.ResetPasswordRequest;
import com.bookshelf.auth.dto.TokenRequest;

@RestController
@RequestMapping( "/api/auth" )
public class AuthController {

    private final AuthService authService;

    public AuthController( AuthService authService ) {
        this.authService = authService;
    }

    @PostMapping( "/register" )
    @ResponseStatus( HttpStatus.ACCEPTED )
    public void register( @RequestBody RegisterRequest request ) {
        authService.register( request );
    }

    @PostMapping( "/activate" )
    public void activate( @RequestBody TokenRequest request ) {
        authService.activate( request.token() );
    }

    @PostMapping( "/login" )
    public LoginResponse login( @RequestBody LoginRequest request ) {
        return authService.login( request );
    }

    @PostMapping( "/forgot-password" )
    @ResponseStatus( HttpStatus.ACCEPTED )
    public void forgotPassword( @RequestBody ForgotPasswordRequest request ) {
        authService.forgotPassword( request.email() );
    }

    @PostMapping( "/reset-password" )
    public void resetPassword( @RequestBody ResetPasswordRequest request ) {
        authService.resetPassword( request );
    }
}
