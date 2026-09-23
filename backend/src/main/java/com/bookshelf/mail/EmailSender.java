package com.bookshelf.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Delivers queued emails once a second. With app.mail.enabled=false ( the
 * integration tests ) jobs are marked sent without touching SMTP.
 */
@Component
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger( EmailSender.class );

    private final EmailJobRepository emailJobRepository;
    private final JavaMailSender mailSender;
    private final boolean enabled;

    public EmailSender( EmailJobRepository emailJobRepository, JavaMailSender mailSender,
            @Value( "${app.mail.enabled}" ) boolean enabled ) {
        this.emailJobRepository = emailJobRepository;
        this.mailSender = mailSender;
        this.enabled = enabled;
    }

    @Scheduled( fixedDelay = 1000 )
    @Transactional
    public void deliverPending() {
        for ( EmailJob job : emailJobRepository.findBySentFalseOrderByCreatedAtAsc() ) {
            if ( enabled ) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom( "no-reply@bookshelf.example" );
                message.setTo( job.getRecipient() );
                message.setSubject( job.getSubject() );
                message.setText( job.getBody() );
                mailSender.send( message );
                log.info( "Sent '{}' to {}", job.getSubject(), job.getRecipient() );
            }
            job.setSent( true );
        }
    }
}
