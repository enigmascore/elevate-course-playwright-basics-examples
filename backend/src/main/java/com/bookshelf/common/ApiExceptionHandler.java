package com.bookshelf.common;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler( ValidationException.class )
    public ResponseEntity<Map<String, Object>> validation( ValidationException e ) {
        return ResponseEntity.status( HttpStatus.UNPROCESSABLE_CONTENT )
                .body( Map.of( "errors", e.getErrors() ) );
    }

    @ExceptionHandler( NotFoundException.class )
    public ResponseEntity<Map<String, Object>> notFound( NotFoundException e ) {
        return ResponseEntity.status( HttpStatus.NOT_FOUND )
                .body( Map.of( "message", e.getMessage() ) );
    }

    @ExceptionHandler( ConflictException.class )
    public ResponseEntity<Map<String, Object>> conflict( ConflictException e ) {
        return ResponseEntity.status( HttpStatus.CONFLICT )
                .body( Map.of( "message", e.getMessage() ) );
    }

    /** a body the JSON parser rejects is a 400 with a message - not an error-page redirect */
    @ExceptionHandler( HttpMessageNotReadableException.class )
    public ResponseEntity<Map<String, Object>> unreadable( HttpMessageNotReadableException e ) {
        return ResponseEntity.status( HttpStatus.BAD_REQUEST )
                .body( Map.of( "message", "The request body could not be read" ) );
    }

    @ExceptionHandler( AuthException.class )
    public ResponseEntity<Map<String, Object>> auth( AuthException e ) {
        return ResponseEntity.status( HttpStatus.UNAUTHORIZED )
                .body( Map.of( "code", e.getCode(), "message", e.getMessage() ) );
    }
}
