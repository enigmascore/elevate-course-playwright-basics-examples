package com.bookshelf.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.bookshelf.testsupport.ApiTestSupport;

@SuppressWarnings( { "rawtypes", "unchecked" } )
class LoanIT extends ApiTestSupport {

    @Test
    void theFourSeededLoansComeBackAfterTheDeliberateDelay() {
        HttpEntity<Void> asBob = new HttpEntity<>( authHeaders( bearer( "bob@example.com", "pageturner" ) ) );

        long started = System.currentTimeMillis();
        ResponseEntity<List> response = rest.exchange( url( "/api/loans" ), HttpMethod.GET, asBob, List.class );
        long elapsed = System.currentTimeMillis() - started;

        assertEquals( HttpStatus.OK, response.getStatusCode() );
        List<Map> loans = response.getBody();
        assertEquals( 4, loans.size() );
        assertEquals( "The Hobbit", loans.get( 0 ).get( "bookTitle" ) );
        assertEquals( "Carol", loans.get( 0 ).get( "borrower" ) );
        // integrationtest profile sets app.loans.delay-ms to 50
        assertTrue( elapsed >= 50, "expected the configured delay, took " + elapsed + "ms" );
    }
}
