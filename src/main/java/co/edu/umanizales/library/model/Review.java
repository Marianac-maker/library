package co.edu.umanizales.library.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private long id;
    private User user;
    private Book book;
    private int rating; // 1-5
    private String comment;
    private LocalDate reviewDate;
}
