package co.edu.umanizales.library.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FineRecord {
    private Long id;
    private User user;
    private Loan loan;
    private double amount;
    private String reason; // LATE_RETURN, DAMAGE, LOSS
    private LocalDate issueDate;
    private LocalDate dueDate;
    private boolean paid;
    private LocalDate paymentDate;
}
