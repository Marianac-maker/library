package co.edu.umanizales.library.service.impl;

import co.edu.umanizales.library.model.*;
import co.edu.umanizales.library.service.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class LoanServiceImpl implements LoanService {
    private final List<Loan> loans = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private static final String CSV_FILE = "loans.csv";

    // Dependencies for related services (to be autowired)
    private final BookService bookService;
    private final UserService userService;

    public LoanServiceImpl(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
        loadFromFile();
    }

    @Override
    public List<Loan> getAllLoans() {
        return new ArrayList<>(loans);
    }

    @Override
    public Optional<Loan> getLoanById(long id) {
        return loans.stream()
                .filter(loan -> loan.getId() == id)
                .findFirst();
    }

    @Override
    public Loan createLoan(Loan loan) {
        // Validate book exists and has available copies
        Book book = bookService.getBookById(loan.getBook().getIsbn())
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No available copies of the book");
        }

        // Validate user exists
        userService.getUserById(loan.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Set loan details
        loan.setId(idCounter.getAndIncrement());
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusWeeks(2)); // 2 weeks loan period
        loan.setReturned(false);
        loan.setReturnDate(null);

        // Update book available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookService.updateBook(book.getIsbn(), book);

        loans.add(loan);
        saveToFile();
        return loan;
    }

    @Override
    public Loan updateLoan(long id, Loan updatedLoan) {
        return getLoanById(id).map(existingLoan -> {
            // Only allow updating certain fields
            if (updatedLoan.getReturned() != existingLoan.isReturned()) {
                existingLoan.setReturned(updatedLoan.isReturned());
                existingLoan.setReturnDate(updatedLoan.isReturned() ? LocalDate.now() : null);
                
                // Update book available copies if loan status changed
                Book book = existingLoan.getBook();
                if (existingLoan.isReturned()) {
                    book.setAvailableCopies(book.getAvailableCopies() + 1);
                } else {
                    book.setAvailableCopies(Math.max(0, book.getAvailableCopies() - 1));
                }
                bookService.updateBook(book.getIsbn(), book);
            }
            
            existingLoan.setDueDate(updatedLoan.getDueDate());
            saveToFile();
            return existingLoan;
        }).orElse(null);
    }

    @Override
    public boolean deleteLoan(long id) {
        Optional<Loan> loanOpt = getLoanById(id);
        if (loanOpt.isPresent()) {
            Loan loan = loanOpt.get();
            if (!loan.isReturned()) {
                // Return the book if loan is deleted before returning
                Book book = loan.getBook();
                book.setAvailableCopies(book.getAvailableCopies() + 1);
                bookService.updateBook(book.getIsbn(), book);
            }
            boolean removed = loans.removeIf(l -> l.getId() == id);
            if (removed) {
                saveToFile();
            }
            return removed;
        }
        return false;
    }

    @Override
    public boolean returnLoan(long id) {
        return getLoanById(id).map(loan -> {
            if (!loan.isReturned()) {
                loan.setReturned(true);
                loan.setReturnDate(LocalDate.now());
                
                // Update book available copies
                Book book = loan.getBook();
                book.setAvailableCopies(book.getAvailableCopies() + 1);
                bookService.updateBook(book.getIsbn(), book);
                
                saveToFile();
                return true;
            }
            return false;
        }).orElse(false);
    }

    @Override
    public List<Loan> getLoansByUserId(long userId) {
        return loans.stream()
                .filter(loan -> loan.getUser().getId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Loan> getLoansByBookId(String bookId) {
        return loans.stream()
                .filter(loan -> loan.getBook().getIsbn().equals(bookId))
                .collect(Collectors.toList());
    }

    @Override
    public void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            // Write header
            writer.println("id,userId,bookIsbn,loanDate,dueDate,returned,returnDate");
            
            // Write data
            for (Loan loan : loans) {
                writer.println(String.format("%d,%d,%s,%s,%s,%b,%s",
                        loan.getId(),
                        loan.getUser().getId(),
                        escapeCsvField(loan.getBook().getIsbn()),
                        loan.getLoanDate(),
                        loan.getDueDate(),
                        loan.isReturned(),
                        loan.getReturnDate() != null ? loan.getReturnDate().toString() : ""));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving loans to CSV file", e);
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
            if (line == null || !line.startsWith("id")) {
                return;
            }

            loans.clear();
            long maxId = 0;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) {
                    try {
                        long id = Long.parseLong(parts[0]);
                        long userId = Long.parseLong(parts[1]);
                        String bookIsbn = unescapeCsvField(parts[2]);
                        LocalDate loanDate = LocalDate.parse(parts[3]);
                        LocalDate dueDate = LocalDate.parse(parts[4]);
                        boolean returned = Boolean.parseBoolean(parts[5]);
                        LocalDate returnDate = parts.length > 6 && !parts[6].isEmpty() ? LocalDate.parse(parts[6]) : null;

                        // Get user and book from their services
                        User user = userService.getUserById(userId)
                                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
                        Book book = bookService.getBookById(bookIsbn)
                                .orElseThrow(() -> new IllegalStateException("Book not found: " + bookIsbn));

                        Loan loan = new Loan(id, user, book, loanDate, dueDate, returned, returnDate);
                        loans.add(loan);

                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing loan: " + e.getMessage());
                    }
                }
            }
            
            idCounter.set(maxId + 1);
        } catch (IOException e) {
            throw new RuntimeException("Error loading loans from CSV file", e);
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
