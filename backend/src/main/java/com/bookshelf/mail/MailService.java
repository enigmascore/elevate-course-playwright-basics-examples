package com.bookshelf.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Composes the app's emails and QUEUES them; EmailSender delivers them later. */
@Service
public class MailService {

    private final EmailJobRepository emailJobRepository;
    private final String frontendUrl;

    public MailService( EmailJobRepository emailJobRepository,
            @Value( "${app.frontend.url}" ) String frontendUrl ) {
        this.emailJobRepository = emailJobRepository;
        this.frontendUrl = frontendUrl;
    }

    public void sendActivation( String email, String name, String token ) {
        String link = frontendUrl + "/activate?token=" + token;
        queue( email, "Activate your Bookshelf account",
                "Hello " + name + ",\n\nWelcome to Bookshelf. Activate your account here:\n\n"
                        + link + "\n\nHappy reading." );
    }

    public void sendPasswordReset( String email, String name, String token ) {
        String link = frontendUrl + "/reset-password?token=" + token;
        queue( email, "Reset your Bookshelf password",
                "Hello " + name + ",\n\nSomeone asked to reset your password. If that was you, "
                        + "choose a new one here:\n\n" + link
                        + "\n\nIf it was not you, ignore this email." );
    }

    private void queue( String recipient, String subject, String body ) {
        emailJobRepository.save( new EmailJob( recipient, subject, body ) );
    }
}
