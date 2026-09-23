package com.bookshelf.mail;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailJobRepository extends JpaRepository<EmailJob, Long> {

    List<EmailJob> findBySentFalseOrderByCreatedAtAsc();
}
