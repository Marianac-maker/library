package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.Loan;
import java.util.List;

public interface LoanService {
    List<Loan> getAllLoans();
    Loan getLoanById(long id);
    Loan createLoan(Loan loan);
    Loan updateLoan(long id, Loan loan);
    boolean deleteLoan(long id);
    boolean returnLoan(long id);
    List<Loan> getLoansByUserId(long userId);
    List<Loan> getLoansByBookId(String bookId);
    void saveToFile();
    void loadFromFile();
}
