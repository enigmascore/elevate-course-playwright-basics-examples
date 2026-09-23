package com.bookshelf.book;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table( name = "books" )
public class Book {

    @Id
    @GeneratedValue( strategy = GenerationType.SEQUENCE, generator = "books_seq" )
    @SequenceGenerator( name = "books_seq", sequenceName = "books_seq", allocationSize = 1 )
    private Long id;

    @Column( nullable = false )
    private String title;

    @Column( nullable = false )
    private String author;

    @Column( nullable = false )
    private String genre;

    @Column( nullable = false )
    private boolean alreadyRead;

    /** the optional cover image, stored as bytes; served by GET /api/books/{id}/cover */
    @Basic( fetch = FetchType.LAZY )
    private byte[] cover;

    private String coverContentType;

    protected Book() {
    }

    public Book( String title, String author, String genre, boolean alreadyRead ) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.alreadyRead = alreadyRead;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public boolean isAlreadyRead() { return alreadyRead; }
    public byte[] getCover() { return cover; }
    public String getCoverContentType() { return coverContentType; }

    public void setCover( byte[] cover, String contentType ) {
        this.cover = cover;
        this.coverContentType = contentType;
    }

    public boolean hasCover() {
        return coverContentType != null;
    }
}
