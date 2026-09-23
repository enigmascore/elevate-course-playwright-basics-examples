package com.bookshelf.book;

/** What the API returns for a book - the cover bytes travel separately. */
public record BookView( Long id, String title, String author, String genre,
        boolean alreadyRead, boolean hasCover ) {

    static BookView of( Book book ) {
        return new BookView( book.getId(), book.getTitle(), book.getAuthor(), book.getGenre(),
                book.isAlreadyRead(), book.hasCover() );
    }
}
