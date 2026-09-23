package com.bookshelf.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.bookshelf.testsupport.ApiTestSupport;

/** The books endpoints as alice, the same behaviour the Playwright suite drives. */
@SuppressWarnings( { "rawtypes", "unchecked" } )
class BookIT extends ApiTestSupport {

    private HttpEntity<Void> asAlice() {
        return new HttpEntity<>( authHeaders( bearer( "alice@example.com", "bookworm" ) ) );
    }

    @Test
    void theSeededBooksAreListedAlphabetically() {
        ResponseEntity<List> response = rest.exchange( url( "/api/books" ), HttpMethod.GET, asAlice(), List.class );

        assertEquals( HttpStatus.OK, response.getStatusCode() );
        List<Map> books = response.getBody();
        assertTrue( books.size() >= 6 );
        assertEquals( "A Brief History of Time", books.get( 0 ).get( "title" ) );
        assertTrue( books.stream().anyMatch( b -> b.get( "title" ).equals( "The Hobbit" ) ) );
    }

    @Test
    void searchMatchesTitleOrAuthorCaseInsensitively() {
        List<Map> byTitle = rest.exchange( url( "/api/books?q=HOBBIT" ), HttpMethod.GET, asAlice(), List.class ).getBody();
        List<Map> byAuthor = rest.exchange( url( "/api/books?q=dahl" ), HttpMethod.GET, asAlice(), List.class ).getBody();

        assertEquals( 1, byTitle.size() );
        assertEquals( "The Hobbit", byTitle.get( 0 ).get( "title" ) );
        assertEquals( "Matilda", byAuthor.get( 0 ).get( "title" ) );
    }

    @Test
    void addingABookWithACoverThenServingAndDeletingIt() {
        String bearer = bearer( "alice@example.com", "bookworm" );
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add( "title", "Persuasion " + System.nanoTime() );
        form.add( "author", "Jane Austen" );
        form.add( "genre", "Fiction" );
        form.add( "alreadyRead", "true" );
        form.add( "cover", new ByteArrayResource( new byte[] { (byte) 0x89, 'P', 'N', 'G' } ) {
            @Override
            public String getFilename() {
                return "cover.png";
            }
        } );
        HttpHeaders headers = authHeaders( bearer );
        headers.setContentType( MediaType.MULTIPART_FORM_DATA );

        ResponseEntity<Map> created = rest.postForEntity( url( "/api/books" ), new HttpEntity<>( form, headers ), Map.class );
        assertEquals( HttpStatus.CREATED, created.getStatusCode() );
        assertEquals( true, created.getBody().get( "hasCover" ) );
        Number id = (Number) created.getBody().get( "id" );

        // the cover is public ( an <img> cannot send a header )
        ResponseEntity<byte[]> cover = rest.getForEntity( url( "/api/books/" + id + "/cover" ), byte[].class );
        assertEquals( HttpStatus.OK, cover.getStatusCode() );
        assertEquals( MediaType.IMAGE_PNG, cover.getHeaders().getContentType() );
        assertEquals( 4, cover.getBody().length );

        // and it persists in the list
        List<Map> books = rest.exchange( url( "/api/books?q=Persuasion" ), HttpMethod.GET,
                new HttpEntity<>( authHeaders( bearer ) ), List.class ).getBody();
        assertTrue( books.stream().anyMatch( b -> ( (Number) b.get( "id" ) ).longValue() == id.longValue() ) );

        ResponseEntity<Void> deleted = rest.exchange( url( "/api/books/" + id ), HttpMethod.DELETE,
                new HttpEntity<>( authHeaders( bearer ) ), Void.class );
        assertEquals( HttpStatus.NO_CONTENT, deleted.getStatusCode() );
        assertEquals( HttpStatus.NOT_FOUND, rest.getForEntity( url( "/api/books/" + id + "/cover" ), byte[].class ).getStatusCode() );
    }

    @Test
    void addBookValidationReportsEveryField() {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add( "title", "" );
        form.add( "genre", "Poetry" );
        form.add( "cover", new ByteArrayResource( "hello".getBytes() ) {
            @Override
            public String getFilename() {
                return "notes.txt";
            }
        } );
        HttpHeaders headers = authHeaders( bearer( "alice@example.com", "bookworm" ) );
        headers.setContentType( MediaType.MULTIPART_FORM_DATA );

        ResponseEntity<Map> response = rest.postForEntity( url( "/api/books" ), new HttpEntity<>( form, headers ), Map.class );

        assertEquals( HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode() );
        Map errors = (Map) response.getBody().get( "errors" );
        assertEquals( "Title is required", errors.get( "title" ) );
        assertEquals( "Author is required", errors.get( "author" ) );
        assertEquals( "Choose a genre", errors.get( "genre" ) );
        assertEquals( "Cover must be a PNG or JPEG image", errors.get( "cover" ) );
    }

    @Test
    void aBookWithoutACoverHas404ForItsCover() {
        assertEquals( HttpStatus.NOT_FOUND,
                rest.getForEntity( url( "/api/books/1/cover" ), byte[].class ).getStatusCode() );
    }

    @Test
    void deletingAnUnknownBookIs404() {
        assertEquals( HttpStatus.NOT_FOUND, rest.exchange( url( "/api/books/999999" ), HttpMethod.DELETE,
                asAlice(), Void.class ).getStatusCode() );
    }
}
