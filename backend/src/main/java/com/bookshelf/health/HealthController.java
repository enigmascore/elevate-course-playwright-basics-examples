package com.bookshelf.health;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Answers 200 without a JWT once the context is up - the readiness probe. */
@RestController
public class HealthController {

    @GetMapping( "/api/health" )
    public Map<String, String> health() {
        return Map.of( "status", "UP" );
    }
}
