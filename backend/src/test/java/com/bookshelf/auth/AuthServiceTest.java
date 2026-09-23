package com.bookshelf.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bookshelf.auth.dto.LoginRequest;
import com.bookshelf.auth.dto.RegisterRequest;
import com.bookshelf.common.AuthException;
import com.bookshelf.common.ConflictException;
import com.bookshelf.mail.MailService;
import com.bookshelf.user.User;
import com.bookshelf.user.UserRepository;

/** Unit test with Mockito: the login and register rules, repositories mocked. */
class AuthServiceTest {

    private UserRepository users;
    private PasswordEncoder encoder;
    private MailService mail;
    private AuthService service;

    @BeforeEach
    void setUp() {
        users = mock( UserRepository.class );
        encoder = mock( PasswordEncoder.class );
        mail = mock( MailService.class );
        service = new AuthService( users, encoder, new JwtService(
                "unit-test-secret-that-is-long-enough-for-hmac-sha256-signing", 60_000 ),
                mail, new AuthValidator() );
    }

    private User activatedAlice() {
        User alice = new User( "Alice", "alice@example.com", "hash" );
        alice.setActivated( true );
        return alice;
    }

    @Test
    void wrongPasswordIsWrongCredentials() {
        when( users.findByEmailIgnoreCase( "alice@example.com" ) ).thenReturn( Optional.of( activatedAlice() ) );
        when( encoder.matches( "nope", "hash" ) ).thenReturn( false );

        AuthException e = assertThrows( AuthException.class,
                () -> service.login( new LoginRequest( "alice@example.com", "nope" ) ) );

        assertEquals( AuthException.WRONG_CREDENTIALS, e.getCode() );
    }

    @Test
    void unknownEmailIsAlsoWrongCredentialsNotAHint() {
        when( users.findByEmailIgnoreCase( anyString() ) ).thenReturn( Optional.empty() );

        AuthException e = assertThrows( AuthException.class,
                () -> service.login( new LoginRequest( "nobody@example.com", "bookworm" ) ) );

        assertEquals( AuthException.WRONG_CREDENTIALS, e.getCode() );
    }

    @Test
    void anUnactivatedAccountCannotLogInEvenWithTheRightPassword() {
        User carol = new User( "Carol", "carol@example.com", "hash" );
        when( users.findByEmailIgnoreCase( "carol@example.com" ) ).thenReturn( Optional.of( carol ) );
        when( encoder.matches( "longenough", "hash" ) ).thenReturn( true );

        AuthException e = assertThrows( AuthException.class,
                () -> service.login( new LoginRequest( "carol@example.com", "longenough" ) ) );

        assertEquals( AuthException.NOT_ACTIVATED, e.getCode() );
    }

    @Test
    void aGoodLoginReturnsATokenForTheUser() {
        when( users.findByEmailIgnoreCase( "alice@example.com" ) ).thenReturn( Optional.of( activatedAlice() ) );
        when( encoder.matches( "bookworm", "hash" ) ).thenReturn( true );

        var response = service.login( new LoginRequest( "alice@example.com", "bookworm" ) );

        assertEquals( "Alice", response.name() );
        assertEquals( "alice@example.com", response.email() );
    }

    @Test
    void registeringATakenEmailConflictsAndSendsNothing() {
        when( users.findByEmailIgnoreCase( "alice@example.com" ) ).thenReturn( Optional.of( activatedAlice() ) );

        assertThrows( ConflictException.class, () -> service.register(
                new RegisterRequest( "Alice", "alice@example.com", "longenough" ) ) );

        verify( mail, never() ).sendActivation( anyString(), anyString(), anyString() );
    }

    @Test
    void registeringQueuesAnActivationEmail() {
        when( users.findByEmailIgnoreCase( "carol@example.com" ) ).thenReturn( Optional.empty() );
        when( encoder.encode( "longenough" ) ).thenReturn( "hash" );
        when( users.save( any( User.class ) ) ).thenAnswer( i -> i.getArgument( 0 ) );

        service.register( new RegisterRequest( " Carol ", "Carol@Example.com", "longenough" ) );

        verify( mail ).sendActivation( eq( "carol@example.com" ), eq( "Carol" ), anyString() );
    }
}
