package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.Author;
import java.util.List;

public interface AuthorService {
    List<Author> getAllAuthors();
    Author getAuthorById(long id);
    List<Author> searchAuthors(String query);
    Author createAuthor(Author author);
    Author updateAuthor(long id, Author author);
    boolean deleteAuthor(long id);
    void saveToFile();
    void loadFromFile();
}
