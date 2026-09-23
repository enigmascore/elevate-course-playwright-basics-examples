package com.bookshelf.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table( name = "users" )
public class User {

    @Id
    @GeneratedValue( strategy = GenerationType.SEQUENCE, generator = "users_seq" )
    @SequenceGenerator( name = "users_seq", sequenceName = "users_seq", allocationSize = 1 )
    private Long id;

    @Column( nullable = false )
    private String name;

    @Column( nullable = false, unique = true )
    private String email;

    @Column( nullable = false )
    private String passwordHash;

    @Column( nullable = false )
    private boolean activated;

    /** set while an activation or password-reset link is outstanding */
    private String activationToken;
    private String resetToken;

    protected User() {
    }

    public User( String name, String email, String passwordHash ) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash( String passwordHash ) { this.passwordHash = passwordHash; }
    public boolean isActivated() { return activated; }
    public void setActivated( boolean activated ) { this.activated = activated; }
    public String getActivationToken() { return activationToken; }
    public void setActivationToken( String activationToken ) { this.activationToken = activationToken; }
    public String getResetToken() { return resetToken; }
    public void setResetToken( String resetToken ) { this.resetToken = resetToken; }
}
