package com.bookshelf.mail;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Email is sent ASYNCHRONOUSLY: a request queues a job here and a scheduled
 * sender delivers it a moment later. A test therefore has to POLL the inbox -
 * asserting the instant the request returns will fail.
 */
@Entity
@Table( name = "email_jobs" )
public class EmailJob {

    @Id
    @GeneratedValue( strategy = GenerationType.SEQUENCE, generator = "email_jobs_seq" )
    @SequenceGenerator( name = "email_jobs_seq", sequenceName = "email_jobs_seq", allocationSize = 1 )
    private Long id;

    @Column( nullable = false )
    private String recipient;

    @Column( nullable = false )
    private String subject;

    @Column( nullable = false, length = 4000 )
    private String body;

    @Column( nullable = false )
    private boolean sent;

    @Column( nullable = false )
    private Instant createdAt = Instant.now();

    protected EmailJob() {
    }

    public EmailJob( String recipient, String subject, String body ) {
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    public Long getId() { return id; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public boolean isSent() { return sent; }
    public void setSent( boolean sent ) { this.sent = sent; }
    public Instant getCreatedAt() { return createdAt; }
}
