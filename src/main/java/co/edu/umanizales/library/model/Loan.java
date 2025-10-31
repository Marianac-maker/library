package co.edu.umanizales.library.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    private long id;
    private User user;
    private Book book;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private boolean returned;
    private LocalDate returnDate;
}
