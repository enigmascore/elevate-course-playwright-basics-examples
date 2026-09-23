package com.bookshelf.auth;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookshelf.auth.dto.LoginRequest;
import com.bookshelf.auth.dto.LoginResponse;
import com.bookshelf.auth.dto.RegisterRequest;
import com.bookshelf.auth.dto.ResetPasswordRequest;
import com.bookshelf.common.AuthException;
import com.bookshelf.common.ConflictException;
import com.bookshelf.mail.MailService;
import com.bookshelf.user.User;
import com.bookshelf.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;
    private final AuthValidator validator;

    public AuthService( UserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtService jwtService, MailService mailService, AuthValidator validator ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailService = mailService;
        this.validator = validator;
    }

    @Transactional
    public void register( RegisterRequest request ) {
        validator.validateRegister( request );
        String email = request.email().trim().toLowerCase();

        if ( userRepository.findByEmailIgnoreCase( email ).isPresent() ) {
            throw new ConflictException( "An account with that email already exists" );
        }

        User user = new User( request.name().trim(), email,
                passwordEncoder.encode( request.password() ) );
        user.setActivationToken( UUID.randomUUID().toString() );
        userRepository.save( user );

        mailService.sendActivation( email, user.getName(), user.getActivationToken() );
    }

    @Transactional
    public void activate( String token ) {
        User user = userRepository.findByActivationToken( token ).orElseThrow(
                () -> new AuthException( AuthException.BAD_TOKEN, "That activation link is not valid" ) );
        user.setActivated( true );
        user.setActivationToken( null );
    }

    @Transactional( readOnly = true )
    public LoginResponse login( LoginRequest request ) {
        User user = userRepository.findByEmailIgnoreCase( request.email() == null ? "" : request.email().trim() )
                .filter( u -> passwordEncoder.matches( request.password() == null ? "" : request.password(),
                        u.getPasswordHash() ) )
                .orElseThrow( () -> new AuthException( AuthException.WRONG_CREDENTIALS,
                        "Wrong email or password" ) );

        if ( !user.isActivated() ) {
            throw new AuthException( AuthException.NOT_ACTIVATED, "Please activate your account first" );
        }

        return new LoginResponse( jwtService.issue( user.getEmail() ), user.getName(), user.getEmail() );
    }

    /** Always answers 200 - whether the address exists is not revealed. */
    @Transactional
    public void forgotPassword( String email ) {
        validator.validateEmail( email );
        userRepository.findByEmailIgnoreCase( email.trim() ).ifPresent( user -> {
            user.setResetToken( UUID.randomUUID().toString() );
            mailService.sendPasswordReset( user.getEmail(), user.getName(), user.getResetToken() );
        } );
    }

    @Transactional
    public void resetPassword( ResetPasswordRequest request ) {
        validator.validateReset( request );
        User user = userRepository.findByResetToken( request.token() == null ? "" : request.token() )
                .orElseThrow( () -> new AuthException( AuthException.BAD_TOKEN,
                        "That reset link is not valid" ) );
        user.setPasswordHash( passwordEncoder.encode( request.password() ) );
        user.setResetToken( null );
    }
}
