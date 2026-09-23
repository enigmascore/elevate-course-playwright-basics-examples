package com.bookshelf.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-that-is-long-enough-for-hmac-sha256-signing";

    @Test
    void aTokenRoundTripsItsSubject() {
        JwtService service = new JwtService( SECRET, 60_000 );

        String token = service.issue( "alice@example.com" );

        assertEquals( Optional.of( "alice@example.com" ), service.subjectOf( token ) );
    }

    @Test
    void aTamperedTokenIsRejected() {
        JwtService service = new JwtService( SECRET, 60_000 );
        String token = service.issue( "alice@example.com" );

        // change one character of the PAYLOAD ( between the two dots ) so the signature no longer matches
        int payloadStart = token.indexOf( '.' ) + 1;
        char original = token.charAt( payloadStart + 5 );
        String tampered = token.substring( 0, payloadStart + 5 ) + ( original == 'a' ? 'b' : 'a' )
                + token.substring( payloadStart + 6 );

        assertTrue( service.subjectOf( tampered ).isEmpty() );
        assertTrue( service.subjectOf( "not.a.jwt" ).isEmpty() );
    }

    @Test
    void anExpiredTokenIsRejected() {
        JwtService service = new JwtService( SECRET, -1_000 );

        assertTrue( service.subjectOf( service.issue( "alice@example.com" ) ).isEmpty() );
    }

    @Test
    void aTokenSignedWithAnotherSecretIsRejected() {
        String token = new JwtService( SECRET, 60_000 ).issue( "alice@example.com" );
        JwtService other = new JwtService( "another-secret-that-is-also-long-enough-for-hmac-sha256", 60_000 );

        assertTrue( other.subjectOf( token ).isEmpty() );
    }
}
