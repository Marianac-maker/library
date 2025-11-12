package co.edu.umanizales.library.service.impl;

import co.edu.umanizales.library.model.*;
import co.edu.umanizales.library.service.AuthorService;
import co.edu.umanizales.library.service.BookService;
import co.edu.umanizales.library.service.CategoryService;
import co.edu.umanizales.library.service.PublisherService;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {
    private final Map<String, Book> bookMap = new HashMap<>();
    private static final String CSV_FILE = "books.csv";

    // Dependencies for related services
    private final AuthorService authorService;
    private final PublisherService publisherService;
    private final CategoryService categoryService;

    public BookServiceImpl(AuthorService authorService, 
                          PublisherService publisherService,
                          CategoryService categoryService) {
        this.authorService = authorService;
        this.publisherService = publisherService;
        this.categoryService = categoryService;
        loadFromFile();
    }

    @Override
    public List<Book> getAllBooks() {
        return new ArrayList<>(bookMap.values());
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        return Optional.ofNullable(bookMap.get(isbn));
    }

    @Override
    public List<Book> searchBooks(String query) {
        String lowerQuery = query.toLowerCase();
        return bookMap.values().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(lowerQuery) ||
                        book.getIsbn().toLowerCase().contains(lowerQuery) ||
                        book.getDescription().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    @Override
    public Book createBook(Book book) {
        // Validate ISBN uniqueness
        if (bookMap.containsKey(book.getIsbn())) {
            throw new IllegalArgumentException("A book with this ISBN already exists");
        }

        // Validate related entities exist
        validateBookRelations(book);

        // Ensure available copies don't exceed total copies
        if (book.getAvailableCopies() > book.getTotalCopies()) {
            book.setAvailableCopies(book.getTotalCopies());
        }

        bookMap.put(book.getIsbn(), book);
        saveToFile();
        return book;
    }

    @Override
    public Book updateBook(String isbn, Book book) {
        if (!bookMap.containsKey(isbn)) {
            return null;
        }

        // If ISBN is being changed, check for uniqueness
        if (!isbn.equals(book.getIsbn()) && bookMap.containsKey(book.getIsbn())) {
            throw new IllegalArgumentException("A book with this ISBN already exists");
        }

        // Validate related entities exist
        validateBookRelations(book);

        // Ensure available copies don't exceed total copies
        if (book.getAvailableCopies() > book.getTotalCopies()) {
            book.setAvailableCopies(book.getTotalCopies());
        }

        // If ISBN changed, remove old entry
        if (!isbn.equals(book.getIsbn())) {
            bookMap.remove(isbn);
        }

        bookMap.put(book.getIsbn(), book);
        saveToFile();
        return book;
    }

    @Override
    public boolean deleteBook(String isbn) {
        if (bookMap.containsKey(isbn)) {
            bookMap.remove(isbn);
            saveToFile();
            return true;
        }
        return false;
    }

    @Override
    public boolean increaseAvailableCopies(String isbn, int count) {
        return getBookByIsbn(isbn).map(book -> {
            int newAvailable = book.getAvailableCopies() + count;
            if (newAvailable > book.getTotalCopies()) {
                book.setAvailableCopies(book.getTotalCopies());
            } else {
                book.setAvailableCopies(newAvailable);
            }
            saveToFile();
            return true;
        }).orElse(false);
    }

    @Override
    public boolean decreaseAvailableCopies(String isbn, int count) {
        return getBookByIsbn(isbn).map(book -> {
            int newAvailable = book.getAvailableCopies() - count;
            if (newAvailable < 0) {
                return false;
            }
            book.setAvailableCopies(newAvailable);
            saveToFile();
            return true;
        }).orElse(false);
    }

    @Override
    public List<Book> getBooksByAuthor(long authorId) {
        return bookMap.values().stream()
                .filter(book -> book.getAuthors().stream()
                        .anyMatch(author -> author.getId() == authorId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> getBooksByPublisher(long publisherId) {
        return bookMap.values().stream()
                .filter(book -> book.getPublisher() != null && 
                        book.getPublisher().getId() == publisherId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> getBooksByCategory(long categoryId) {
        return bookMap.values().stream()
                .filter(book -> book.getCategory() != null && 
                        book.getCategory().getId() == categoryId)
                .collect(Collectors.toList());
    }

    @Override
    public void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            // Write header
            writer.println("isbn,title,authors,publisherId,publicationYear,edition,categoryId,availableCopies,totalCopies,location,description");
            
            // Write data
            for (Book book : bookMap.values()) {
                String authors = book.getAuthors().stream()
                        .map(author -> String.valueOf(author.getId()))
                        .collect(Collectors.joining("|"));
                
                writer.println(String.format("%s,%s,%s,%d,%d,%d,%d,%d,%d,%s,%s",
                        escapeCsvField(book.getIsbn()),
                        escapeCsvField(book.getTitle()),
                        escapeCsvField(authors),
                        book.getPublisher() != null ? book.getPublisher().getId() : 0,
                        book.getPublicationYear(),
                        book.getEdition(),
                        book.getCategory() != null ? book.getCategory().getId() : 0,
                        book.getAvailableCopies(),
                        book.getTotalCopies(),
                        escapeCsvField(book.getLocation()),
                        escapeCsvField(book.getDescription())
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving books to CSV file", e);
        }
    }

    @Override
    public void loadFromFile() {
        File file = new File(CSV_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip header
            String line = reader.readLine();
            if (line == null || !line.startsWith("isbn")) {
                return;
            }

            bookMap.clear();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 9) {
                    try {
                        String isbn = unescapeCsvField(parts[0]);
                        String title = unescapeCsvField(parts[1]);
                        
                        // Parse authors
                        List<Author> authors = new ArrayList<>();
                        String[] authorIds = parts[2].split("\\|");
                        for (String authorId : authorIds) {
                            if (!authorId.isEmpty()) {
                                authorService.getAuthorById(Long.parseLong(authorId))
                                        .ifPresent(authors::add);
                            }
                        }
                        
                        // Get publisher
                        long publisherId = Long.parseLong(parts[3]);
                        Publisher publisher = publisherService.getPublisherById(publisherId);
                        
                        int publicationYear = Integer.parseInt(parts[4]);
                        int edition = Integer.parseInt(parts[5]);
                        
                        // Get category
                        long categoryId = Long.parseLong(parts[6]);
                        Category category = categoryService.getCategoryById(categoryId).orElse(null);
                        
                        int availableCopies = Integer.parseInt(parts[7]);
                        int totalCopies = Integer.parseInt(parts[8]);
                        String location = parts.length > 9 ? unescapeCsvField(parts[9]) : "";
                        String description = parts.length > 10 ? unescapeCsvField(parts[10]) : "";

                        Book book = new Book(isbn, title, authors, publisher, publicationYear, 
                                edition, category, availableCopies, totalCopies, location, description);
                        
                        bookMap.put(isbn, book);
                    } catch (Exception e) {
                        System.err.println("Error parsing book: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading books from CSV file", e);
        }
    }

    private void validateBookRelations(Book book) {
        // Validate authors exist
        if (book.getAuthors() != null) {
            for (Author author : book.getAuthors()) {
                authorService.getAuthorById(author.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Author not found: " + author.getId()));
            }
        }

        // Validate publisher exists if provided
        if (book.getPublisher() != null) {
            publisherService.getPublisherById(book.getPublisher().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Publisher not found: " + book.getPublisher().getId()));
        }

        // Validate category exists if provided
        if (book.getCategory() != null) {
            categoryService.getCategoryById(book.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + book.getCategory().getId()));
        }
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // Escape quotes by doubling them and wrap in quotes if contains comma or newline
        String escaped = field.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            escaped = "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String unescapeCsvField(String field) {
        if (field == null || field.isEmpty()) {
            return "";
        }
        // Remove surrounding quotes if present
        if (field.startsWith("\"") && field.endsWith("\"")) {
            field = field.substring(1, field.length() - 1);
            // Unescape quotes
            field = field.replace("\"\"", "\"");
        }
        return field;
    }
}
