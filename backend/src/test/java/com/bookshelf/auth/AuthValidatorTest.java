package com.bookshelf.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.bookshelf.auth.dto.RegisterRequest;
import com.bookshelf.common.ValidationException;

/** Unit test: the register rules, no Spring context. */
class AuthValidatorTest {

    private final AuthValidator validator = new AuthValidator();

    @Test
    void aCompleteRegistrationPasses() {
        assertDoesNotThrow( () -> validator.validateRegister(
                new RegisterRequest( "Carol", "carol@example.com", "longenough" ) ) );
    }

    @Test
    void everyMissingFieldIsReportedAtOnce() {
        ValidationException e = assertThrows( ValidationException.class,
                () -> validator.validateRegister( new RegisterRequest( " ", "", null ) ) );

        assertEquals( "Name is required", e.getErrors().get( "name" ) );
        assertEquals( "Email is required", e.getErrors().get( "email" ) );
        assertEquals( "Password is required", e.getErrors().get( "password" ) );
    }

    @Test
    void emailMustLookLikeAnEmail() {
        ValidationException e = assertThrows( ValidationException.class,
                () -> validator.validateRegister( new RegisterRequest( "Carol", "carol", "longenough" ) ) );

        assertEquals( "Email must look like name@example.com", e.getErrors().get( "email" ) );
        assertEquals( 1, e.getErrors().size() );
    }

    @Test
    void passwordMustBeEightCharacters() {
        ValidationException e = assertThrows( ValidationException.class,
                () -> validator.validateRegister( new RegisterRequest( "Carol", "carol@example.com", "short" ) ) );

        assertEquals( "Password must be at least 8 characters", e.getErrors().get( "password" ) );
    }
}
