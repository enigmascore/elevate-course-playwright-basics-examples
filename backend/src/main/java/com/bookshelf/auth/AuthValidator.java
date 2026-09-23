package com.bookshelf.auth;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.bookshelf.auth.dto.RegisterRequest;
import com.bookshelf.auth.dto.ResetPasswordRequest;
import com.bookshelf.common.ValidationException;

/** Field-level rules for the auth forms. The front end mirrors every rule. */
@Component
public class AuthValidator {

    static final Pattern EMAIL = Pattern.compile( "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$" );
    static final int MIN_PASSWORD = 8;

    public void validateRegister( RegisterRequest request ) {
        Map<String, String> errors = new LinkedHashMap<>();

        if ( isBlank( request.name() ) ) {
            errors.put( "name", "Name is required" );
        }
        checkEmail( request.email(), errors );
        checkPassword( request.password(), errors );

        if ( !errors.isEmpty() ) {
            throw new ValidationException( errors );
        }
    }

    public void validateReset( ResetPasswordRequest request ) {
        Map<String, String> errors = new LinkedHashMap<>();
        checkPassword( request.password(), errors );

        if ( !errors.isEmpty() ) {
            throw new ValidationException( errors );
        }
    }

    public void validateEmail( String email ) {
        Map<String, String> errors = new LinkedHashMap<>();
        checkEmail( email, errors );

        if ( !errors.isEmpty() ) {
            throw new ValidationException( errors );
        }
    }

    private void checkEmail( String email, Map<String, String> errors ) {
        if ( isBlank( email ) ) {
            errors.put( "email", "Email is required" );
        }
        else if ( !EMAIL.matcher( email.trim() ).matches() ) {
            errors.put( "email", "Email must look like name@example.com" );
        }
    }

    private void checkPassword( String password, Map<String, String> errors ) {
        if ( isBlank( password ) ) {
            errors.put( "password", "Password is required" );
        }
        else if ( password.length() < MIN_PASSWORD ) {
            errors.put( "password", "Password must be at least " + MIN_PASSWORD + " characters" );
        }
    }

    static boolean isBlank( String value ) {
        return value == null || value.trim().isEmpty();
    }
}
