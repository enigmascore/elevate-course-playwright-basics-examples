package com.bookshelf.loan;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table( name = "loans" )
public class Loan {

    @Id
    @GeneratedValue( strategy = GenerationType.SEQUENCE, generator = "loans_seq" )
    @SequenceGenerator( name = "loans_seq", sequenceName = "loans_seq", allocationSize = 1 )
    private Long id;

    @Column( nullable = false )
    private String bookTitle;

    @Column( nullable = false )
    private String borrower;

    @Column( nullable = false )
    private LocalDate dueDate;

    protected Loan() {
    }

    public Long getId() { return id; }
    public String getBookTitle() { return bookTitle; }
    public String getBorrower() { return borrower; }
    public LocalDate getDueDate() { return dueDate; }
}
