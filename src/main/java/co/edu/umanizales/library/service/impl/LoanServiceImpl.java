package co.edu.umanizales.library.service.impl;

import co.edu.umanizales.library.model.*;
import co.edu.umanizales.library.service.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LoanServiceImpl implements LoanService {
    private final List<Loan> loans = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private static final String CSV_FILE = "data/loans.csv";

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
    public Loan getLoanById(long id) {
        for (Loan loan : loans) {
            if (loan.getId() == id) {
                return loan;
            }
        }
        return null;
    }

    @Override
    public Loan createLoan(Loan loan) {
        // Validate required fields
        if (loan.getUser() == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (loan.getBook() == null) {
            throw new IllegalArgumentException("Book is required");
        }
        if (loan.getBook().getIsbn() == null || loan.getBook().getIsbn().trim().isEmpty()) {
            throw new IllegalArgumentException("Book ISBN is required");
        }

        // Validate book exists and has available copies
        Book book = bookService.getBookByIsbn(loan.getBook().getIsbn());
        if (book == null) {
            throw new IllegalArgumentException("Book not found");
        }
        
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No available copies of the book");
        }

        // Validate user exists
        User user = userService.getUserById(loan.getUser().getId());
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

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
        Loan existingLoan = getLoanById(id);
        if (existingLoan != null) {
            // Only allow updating certain fields
            if (updatedLoan.isReturned() != existingLoan.isReturned()) {
                existingLoan.setReturned(updatedLoan.isReturned());
                existingLoan.setReturnDate(updatedLoan.isReturned() ? LocalDate.now() : null);

                // Update book available copies if loan status changed
                Book b = existingLoan.getBook();
                if (existingLoan.isReturned()) {
                    b.setAvailableCopies(b.getAvailableCopies() + 1);
                } else {
                    b.setAvailableCopies(Math.max(0, b.getAvailableCopies() - 1));
                }
                bookService.updateBook(b.getIsbn(), b);
            }

            existingLoan.setDueDate(updatedLoan.getDueDate());
            saveToFile();
            return existingLoan;
        }
        return null;
    }

    @Override
    public boolean deleteLoan(long id) {
        Loan loan = getLoanById(id);
        if (loan != null) {
            if (!loan.isReturned()) {
                // Return the book if loan is deleted before returning
                Book book = loan.getBook();
                book.setAvailableCopies(book.getAvailableCopies() + 1);
                bookService.updateBook(book.getIsbn(), book);
            }
            boolean removed = false;
            Iterator<Loan> it = loans.iterator();
            while (it.hasNext()) {
                Loan l = it.next();
                if (l.getId() == id) {
                    it.remove();
                    removed = true;
                    break;
                }
            }
            if (removed) {
                saveToFile();
            }
            return removed;
        }
        return false;
    }

    @Override
    public boolean returnLoan(long id) {
        Loan loan = getLoanById(id);
        if (loan != null) {
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
        }
        return false;
    }

    @Override
    public List<Loan> getLoansByUserId(long userId) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.getUser() != null && loan.getUser().getId() == userId) {
                result.add(loan);
            }
        }
        return result;
    }

    @Override
    public List<Loan> getLoansByBookId(String bookId) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.getBook() != null && loan.getBook().getIsbn() != null && loan.getBook().getIsbn().equals(bookId)) {
                result.add(loan);
            }
        }
        return result;
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
                        User userLoaded = userService.getUserById(userId);
                        if (userLoaded == null) {
                            throw new IllegalStateException("User not found: " + userId);
                        }
                        Book bookLoaded = bookService.getBookByIsbn(bookIsbn);
                        if (bookLoaded == null) {
                            throw new IllegalStateException("Book not found: " + bookIsbn);
                        }

                        Loan loan = new Loan(id, userLoaded, bookLoaded, loanDate, dueDate, returned, returnDate);
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
