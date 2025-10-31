package co.edu.umanizales.library.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Return {
    private Long id;
    private Loan loan;
    private LocalDateTime returnDate;
    private String condition; // GOOD, DAMAGED, LOST
    private String notes;
    private double fineAmount;
    private boolean finePaid;
}
