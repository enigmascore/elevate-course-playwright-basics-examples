package com.bookshelf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bookshelf - the small full-stack app the Basic Playwright course tests against.
 * Students RUN this backend ( in docker ); they never need to read it.
 */
@SpringBootApplication
@EnableScheduling
public class BookshelfApplication {

    public static void main( String[] args ) {
        SpringApplication.run( BookshelfApplication.class, args );
    }
}
