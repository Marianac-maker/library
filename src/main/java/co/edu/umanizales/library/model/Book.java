package co.edu.umanizales.library.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private String isbn;
    private String title;
    private List<Author> authors;
    private Publisher publisher;
    private int publicationYear;
    private int edition;
    private Category category;
    private int availableCopies;
    private int totalCopies;
    private String location;
    private String description;
}
