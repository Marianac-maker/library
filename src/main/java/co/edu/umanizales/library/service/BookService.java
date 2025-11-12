package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookService {
    List<Book> getAllBooks();
    Optional<Book> getBookByIsbn(String isbn);
    List<Book> searchBooks(String query);
    Book createBook(Book book);
    Book updateBook(String isbn, Book book);
    boolean deleteBook(String isbn);
    boolean increaseAvailableCopies(String isbn, int count);
    boolean decreaseAvailableCopies(String isbn, int count);
    List<Book> getBooksByAuthor(long authorId);
    List<Book> getBooksByPublisher(long publisherId);
    List<Book> getBooksByCategory(long categoryId);
    void saveToFile();
    void loadFromFile();
}
