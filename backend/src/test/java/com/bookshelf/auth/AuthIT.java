package com.bookshelf.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.bookshelf.mail.EmailJob;
import com.bookshelf.mail.EmailJobRepository;
import com.bookshelf.testsupport.ApiTestSupport;
import com.bookshelf.user.UserRepository;

/** Every auth endpoint against the real context; emails are read from the queue. */
@SuppressWarnings( { "rawtypes", "unchecked" } )
class AuthIT extends ApiTestSupport {

    @Autowired
    private EmailJobRepository emailJobs;

    @Autowired
    private UserRepository users;

    @Test
    void aliceLogsInAndGetsAToken() {
        ResponseEntity<Map> response = rest.postForEntity( url( "/api/auth/login" ),
                json( Map.of( "email", "alice@example.com", "password", "bookworm" ) ), Map.class );

        assertEquals( HttpStatus.OK, response.getStatusCode() );
        assertEquals( "Alice", response.getBody().get( "name" ) );
        assertNotNull( response.getBody().get( "token" ) );
    }

    @Test
    void aWrongPasswordIs401WithTheWrongCredentialsCode() {
        ResponseEntity<Map> response = rest.postForEntity( url( "/api/auth/login" ),
                json( Map.of( "email", "alice@example.com", "password", "nope" ) ), Map.class );

        assertEquals( HttpStatus.UNAUTHORIZED, response.getStatusCode() );
        assertEquals( "WRONG_CREDENTIALS", response.getBody().get( "code" ) );
        assertEquals( "Wrong email or password", response.getBody().get( "message" ) );
    }

    @Test
    void registerValidationReportsEveryField() {
        ResponseEntity<Map> response = rest.postForEntity( url( "/api/auth/register" ),
                json( Map.of( "name", "", "email", "carol", "password", "short" ) ), Map.class );

        assertEquals( HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode() );
        Map errors = (Map) response.getBody().get( "errors" );
        assertEquals( "Name is required", errors.get( "name" ) );
        assertEquals( "Email must look like name@example.com", errors.get( "email" ) );
        assertEquals( "Password must be at least 8 characters", errors.get( "password" ) );
    }

    @Test
    void registerActivateLoginJourney() {
        String email = "carol." + System.nanoTime() + "@example.com";

        ResponseEntity<Void> registered = rest.postForEntity( url( "/api/auth/register" ),
                json( Map.of( "name", "Carol", "email", email, "password", "longenough" ) ), Void.class );
        assertEquals( HttpStatus.ACCEPTED, registered.getStatusCode() );

        // not yet activated: the right password is refused with the NOT_ACTIVATED code
        ResponseEntity<Map> early = rest.postForEntity( url( "/api/auth/login" ),
                json( Map.of( "email", email, "password", "longenough" ) ), Map.class );
        assertEquals( HttpStatus.UNAUTHORIZED, early.getStatusCode() );
        assertEquals( "NOT_ACTIVATED", early.getBody().get( "code" ) );

        // the activation email was QUEUED with a link carrying the token
        EmailJob job = emailJobs.findAll().stream()
                .filter( j -> j.getRecipient().equals( email ) ).findFirst().orElseThrow();
        assertEquals( "Activate your Bookshelf account", job.getSubject() );
        assertTrue( job.getBody().contains( "http://localhost:5173/activate?token=" ) );
        String token = users.findByEmailIgnoreCase( email ).orElseThrow().getActivationToken();
        assertTrue( job.getBody().contains( token ) );

        assertEquals( HttpStatus.OK, rest.postForEntity( url( "/api/auth/activate" ),
                json( Map.of( "token", token ) ), Void.class ).getStatusCode() );

        ResponseEntity<Map> login = rest.postForEntity( url( "/api/auth/login" ),
                json( Map.of( "email", email, "password", "longenough" ) ), Map.class );
        assertEquals( HttpStatus.OK, login.getStatusCode() );
        assertEquals( "Carol", login.getBody().get( "name" ) );
    }

    @Test
    void registeringATakenEmailIsAConflict() {
        ResponseEntity<Map> response = rest.postForEntity( url( "/api/auth/register" ),
                json( Map.of( "name", "Alice", "email", "alice@example.com", "password", "longenough" ) ), Map.class );

        assertEquals( HttpStatus.CONFLICT, response.getStatusCode() );
    }

    @Test
    void forgottenPasswordJourney() {
        // bob asks for a reset; the email carries a reset link with his token
        assertEquals( HttpStatus.ACCEPTED, rest.postForEntity( url( "/api/auth/forgot-password" ),
                json( Map.of( "email", "bob@example.com" ) ), Void.class ).getStatusCode() );

        String token = users.findByEmailIgnoreCase( "bob@example.com" ).orElseThrow().getResetToken();
        assertNotNull( token );
        EmailJob job = emailJobs.findAll().stream()
                .filter( j -> j.getRecipient().equals( "bob@example.com" ) ).findFirst().orElseThrow();
        assertTrue( job.getBody().contains( "/reset-password?token=" + token ) );

        // a short new password is refused; a proper one is accepted and logs in
        ResponseEntity<Map> tooShort = rest.postForEntity( url( "/api/auth/reset-password" ),
                json( Map.of( "token", token, "password", "short" ) ), Map.class );
        assertEquals( HttpStatus.UNPROCESSABLE_CONTENT, tooShort.getStatusCode() );

        assertEquals( HttpStatus.OK, rest.postForEntity( url( "/api/auth/reset-password" ),
                json( Map.of( "token", token, "password", "turnthepage" ) ), Void.class ).getStatusCode() );
        assertEquals( HttpStatus.OK, rest.postForEntity( url( "/api/auth/login" ),
                json( Map.of( "email", "bob@example.com", "password", "turnthepage" ) ), Map.class ).getStatusCode() );

        // the token is single-use
        assertEquals( HttpStatus.UNAUTHORIZED, rest.postForEntity( url( "/api/auth/reset-password" ),
                json( Map.of( "token", token, "password", "turnthepage" ) ), Map.class ).getStatusCode() );
    }

    @Test
    void forgottenPasswordForAnUnknownEmailStillAnswers202() {
        assertEquals( HttpStatus.ACCEPTED, rest.postForEntity( url( "/api/auth/forgot-password" ),
                json( Map.of( "email", "nobody@example.com" ) ), Void.class ).getStatusCode() );
    }
}
