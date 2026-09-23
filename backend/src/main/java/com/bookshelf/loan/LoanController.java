package com.bookshelf.loan;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The loans list answers SLOWLY on purpose ( app.loans.delay-ms ), so the
 * front end shows "Loading loans..." first and a test has something to wait for.
 */
@RestController
public class LoanController {

    private final LoanRepository loanRepository;
    private final long delayMs;

    public LoanController( LoanRepository loanRepository,
            @Value( "${app.loans.delay-ms}" ) long delayMs ) {
        this.loanRepository = loanRepository;
        this.delayMs = delayMs;
    }

    @GetMapping( "/api/loans" )
    public List<Loan> loans() throws InterruptedException {
        Thread.sleep( delayMs );
        return loanRepository.findAllByOrderByDueDateAsc();
    }
}
