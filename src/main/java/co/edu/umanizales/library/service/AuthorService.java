package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.Author;
import java.util.List;
import java.util.Optional;

public interface AuthorService {
    List<Author> getAllAuthors();
    Optional<Author> getAuthorById(long id);
    List<Author> searchAuthors(String query);
    Author createAuthor(Author author);
    Author updateAuthor(long id, Author author);
    boolean deleteAuthor(long id);
    void saveToFile();
    void loadFromFile();
}
