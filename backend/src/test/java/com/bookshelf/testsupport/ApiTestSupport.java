package com.bookshelf.testsupport;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

/** Shared shape of every *IT: real context, random port, TestRestTemplate. */
@AutoConfigureTestRestTemplate
@SpringBootTest( webEnvironment = WebEnvironment.RANDOM_PORT )
@ActiveProfiles( "integrationtest" )
public abstract class ApiTestSupport {

    @Autowired
    protected TestRestTemplate rest;

    @LocalServerPort
    protected int port;

    protected String url( String path ) {
        return "http://localhost:" + port + path;
    }

    protected static HttpEntity<Object> json( Object body ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        return new HttpEntity<>( body, headers );
    }

    /** logs in through the real endpoint and returns "Bearer ..." */
    @SuppressWarnings( "unchecked" )
    protected String bearer( String email, String password ) {
        Map<String, Object> body = rest.postForObject( url( "/api/auth/login" ),
                json( Map.of( "email", email, "password", password ) ), Map.class );
        return "Bearer " + body.get( "token" );
    }

    protected static HttpHeaders authHeaders( String bearer ) {
        HttpHeaders headers = new HttpHeaders();
        headers.set( HttpHeaders.AUTHORIZATION, bearer );
        return headers;
    }
}
