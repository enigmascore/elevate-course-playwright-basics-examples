package com.bookshelf.health;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.bookshelf.testsupport.ApiTestSupport;

class HealthIT extends ApiTestSupport {

    @Test
    @SuppressWarnings( "rawtypes" )
    void healthAnswersWithoutAToken() {
        ResponseEntity<Map> response = rest.getForEntity( url( "/api/health" ), Map.class );

        assertEquals( HttpStatus.OK, response.getStatusCode() );
        assertEquals( "UP", response.getBody().get( "status" ) );
    }

    @Test
    void everythingElseNeedsAToken() {
        assertEquals( HttpStatus.UNAUTHORIZED,
                rest.getForEntity( url( "/api/books" ), String.class ).getStatusCode() );
        assertEquals( HttpStatus.UNAUTHORIZED,
                rest.getForEntity( url( "/api/loans" ), String.class ).getStatusCode() );
    }
}
