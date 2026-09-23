import { useEffect, useState } from "react";

import { api, type Loan } from "../api";

// The backend answers this list SLOWLY on purpose ( ~1.5 s ): the page shows a
// loading state first, so a test has something real to wait for.
export default function LoansPage() {
  const [loans, setLoans] = useState<Loan[] | null>(null);

  useEffect(() => {
    let cancelled = false;
    void api.loans().then((result) => !cancelled && setLoans(result));
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <>
      <h1>Loans</h1>
      {loans === null ? (
        <p className="muted">Loading loans...</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Book</th>
              <th>Borrower</th>
              <th>Due</th>
            </tr>
          </thead>
          <tbody>
            {loans.map((loan) => (
              <tr key={loan.id} data-testid="loan-row">
                <td>{loan.bookTitle}</td>
                <td>{loan.borrower}</td>
                <td>{loan.dueDate}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}
