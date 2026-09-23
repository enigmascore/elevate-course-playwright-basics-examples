package com.bookshelf.book;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping( "/api/books" )
public class BookController {

    private final BookService bookService;

    public BookController( BookService bookService ) {
        this.bookService = bookService;
    }

    /** GET /api/books?q=hobbit - a REAL request the network lesson intercepts */
    @GetMapping
    public List<BookView> list( @RequestParam( required = false ) String q ) {
        return bookService.list( q );
    }

    /** multipart, because the cover is an optional file */
    @PostMapping( consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
    @ResponseStatus( HttpStatus.CREATED )
    public BookView add( @RequestParam( required = false ) String title,
            @RequestParam( required = false ) String author,
            @RequestParam( required = false ) String genre,
            @RequestParam( defaultValue = "false" ) boolean alreadyRead,
            @RequestParam( required = false ) MultipartFile cover ) {
        return bookService.add( title, author, genre, alreadyRead, cover );
    }

    @DeleteMapping( "/{id}" )
    @ResponseStatus( HttpStatus.NO_CONTENT )
    public void delete( @PathVariable Long id ) {
        bookService.delete( id );
    }

    @GetMapping( "/{id}/cover" )
    public ResponseEntity<byte[]> cover( @PathVariable Long id ) {
        Book book = bookService.coverOf( id );
        return ResponseEntity.ok()
                .contentType( MediaType.parseMediaType( book.getCoverContentType() ) )
                .body( book.getCover() );
    }
}
