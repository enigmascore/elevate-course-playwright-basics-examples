package com.bookshelf.book;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.bookshelf.common.ValidationException;

/** The Add book rules. The front end mirrors every one of them. */
@Component
public class BookValidator {

    public static final List<String> GENRES =
            List.of( "Fiction", "Non-fiction", "Science", "History", "Children" );

    static final List<String> COVER_TYPES = List.of( "image/png", "image/jpeg" );

    public void validate( String title, String author, String genre, MultipartFile cover ) {
        Map<String, String> errors = new LinkedHashMap<>();

        if ( isBlank( title ) ) {
            errors.put( "title", "Title is required" );
        }
        if ( isBlank( author ) ) {
            errors.put( "author", "Author is required" );
        }
        if ( isBlank( genre ) || !GENRES.contains( genre ) ) {
            errors.put( "genre", "Choose a genre" );
        }
        if ( cover != null && !cover.isEmpty()
                && !COVER_TYPES.contains( cover.getContentType() ) ) {
            errors.put( "cover", "Cover must be a PNG or JPEG image" );
        }

        if ( !errors.isEmpty() ) {
            throw new ValidationException( errors );
        }
    }

    static boolean isBlank( String value ) {
        return value == null || value.trim().isEmpty();
    }
}
