package com.bookshelf.book;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.bookshelf.common.ValidationException;

class BookValidatorTest {

    private final BookValidator validator = new BookValidator();

    @Test
    void titleAuthorAndGenreAreRequired() {
        ValidationException e = assertThrows( ValidationException.class,
                () -> validator.validate( "", " ", null, null ) );

        assertEquals( "Title is required", e.getErrors().get( "title" ) );
        assertEquals( "Author is required", e.getErrors().get( "author" ) );
        assertEquals( "Choose a genre", e.getErrors().get( "genre" ) );
    }

    @Test
    void theGenreMustBeOneOfTheKnownFive() {
        ValidationException e = assertThrows( ValidationException.class,
                () -> validator.validate( "Dune", "Frank Herbert", "Poetry", null ) );

        assertEquals( "Choose a genre", e.getErrors().get( "genre" ) );
        assertEquals( 1, e.getErrors().size() );
    }

    @Test
    void aCoverMustBeAnImage() {
        MockMultipartFile text = new MockMultipartFile( "cover", "notes.txt", "text/plain", "hi".getBytes() );

        ValidationException e = assertThrows( ValidationException.class,
                () -> validator.validate( "Dune", "Frank Herbert", "Fiction", text ) );

        assertEquals( "Cover must be a PNG or JPEG image", e.getErrors().get( "cover" ) );
    }

    @Test
    void aPngCoverOrNoCoverIsFine() {
        MockMultipartFile png = new MockMultipartFile( "cover", "c.png", "image/png", new byte[] { 1 } );

        assertDoesNotThrow( () -> validator.validate( "Dune", "Frank Herbert", "Fiction", png ) );
        assertDoesNotThrow( () -> validator.validate( "Dune", "Frank Herbert", "Fiction", null ) );
    }
}
