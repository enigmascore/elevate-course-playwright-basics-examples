package com.bookshelf.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase( String email );

    Optional<User> findByActivationToken( String token );

    Optional<User> findByResetToken( String token );
}
