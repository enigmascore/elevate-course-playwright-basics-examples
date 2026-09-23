package com.bookshelf.book;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bookshelf.common.NotFoundException;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookValidator validator;

    public BookService( BookRepository bookRepository, BookValidator validator ) {
        this.bookRepository = bookRepository;
        this.validator = validator;
    }

    @Transactional( readOnly = true )
    public List<BookView> list( String q ) {
        List<Book> books = ( q == null || q.trim().isEmpty() )
                ? bookRepository.findAllByOrderByTitleAsc()
                : bookRepository.search( q.trim() );
        return books.stream().map( BookView::of ).toList();
    }

    @Transactional
    public BookView add( String title, String author, String genre, boolean alreadyRead,
            MultipartFile cover ) {
        validator.validate( title, author, genre, cover );
        Book book = new Book( title.trim(), author.trim(), genre, alreadyRead );

        if ( cover != null && !cover.isEmpty() ) {
            try {
                book.setCover( cover.getBytes(), cover.getContentType() );
            }
            catch ( IOException e ) {
                throw new IllegalStateException( "Could not read the uploaded cover", e );
            }
        }

        return BookView.of( bookRepository.save( book ) );
    }

    @Transactional
    public void delete( Long id ) {
        Book book = bookRepository.findById( id )
                .orElseThrow( () -> new NotFoundException( "No book with id " + id ) );
        bookRepository.delete( book );
    }

    @Transactional( readOnly = true )
    public Book coverOf( Long id ) {
        return bookRepository.findById( id ).filter( Book::hasCover )
                .orElseThrow( () -> new NotFoundException( "Book " + id + " has no cover" ) );
    }
}
