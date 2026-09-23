package com.bookshelf.common;

import java.util.Map;

/** One message per invalid field; the API answers 422 with { "errors": { field: message } }. */
public class ValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public ValidationException( Map<String, String> errors ) {
        super( "Validation failed: " + errors );
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
