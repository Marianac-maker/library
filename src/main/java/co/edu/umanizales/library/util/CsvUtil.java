package co.edu.umanizales.library.util;

import co.edu.umanizales.library.model.User;
import co.edu.umanizales.library.model.Review;
import co.edu.umanizales.library.model.UserRole;
import co.edu.umanizales.library.model.Book;
import co.edu.umanizales.library.model.Publisher;
import co.edu.umanizales.library.model.Return;
import co.edu.umanizales.library.model.Loan;
import co.edu.umanizales.library.model.BookCondition;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvUtil {

    private static final String CSV_HEADER = "id,name,email,phoneNumber,username,password,role,active";

    public static List<User> readUsersFromCsv(String filePath) throws IOException {
        List<User> users = new ArrayList<>();

        if (!Files.exists(Paths.get(filePath))) {
            log.warn("CSV file not found: {}", filePath);
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                String[] fields = line.split(",");
                if (fields.length >= 8) {
                    try {
                        User user = new User();
                        user.setId(Long.parseLong(fields[0]));
                        user.setName(fields[1]);
                        user.setEmail(fields[2]);
                        user.setPhoneNumber(fields[3]);
                        user.setUsername(fields[4]);
                        user.setPassword(fields[5]);
                        user.setRole(parseUserRole(fields[6]));
                        user.setActive(Boolean.parseBoolean(fields[7]));
                        users.add(user);
                    } catch (NumberFormatException e) {
                        log.error("Error parsing user line: {}", line, e);
                    }
                }
            }
        }

        log.info("Loaded {} users from CSV", users.size());
        return users;
    }

    public static void writeUsersToCsv(String filePath, List<User> users) throws IOException {
        // Create directory if it doesn't exist
        Files.createDirectories(Paths.get(filePath).getParent());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header
            writer.write(CSV_HEADER);
            writer.newLine();

            // Write user data
            for (User user : users) {
                String roleStr = user.getRole() != null ? user.getRole().name() : "";
                String line = String.format("%d,%s,%s,%s,%s,%s,%s,%b",
                        user.getId(),
                        escapeCsv(user.getName()),
                        escapeCsv(user.getEmail()),
                        escapeCsv(user.getPhoneNumber()),
                        escapeCsv(user.getUsername()),
                        escapeCsv(user.getPassword()),
                        escapeCsv(roleStr),
                        user.isActive());
                writer.write(line);
                writer.newLine();
            }
        }

        log.info("Wrote {} users to CSV", users.size());
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static UserRole parseUserRole(String value) {
        if (value == null) return null;
        String normalized = value.trim().toUpperCase();
        if (normalized.isEmpty()) return null;
        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown UserRole in CSV: {}", value);
            return null;
        }
    }

    private static BookCondition parseBookCondition(String value) {
        if (value == null) return null;
        String normalized = value.trim().toUpperCase();
        if (normalized.isEmpty()) return null;
        try {
            return BookCondition.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown BookCondition in CSV: {}", value);
            return null;
        }
    }

    private static final String REVIEWS_CSV_HEADER = "id,userId,bookIsbn,rating,comment,reviewDate";

    public static List<Review> readReviewsFromCsv(String filePath) throws IOException {
        List<Review> reviews = new ArrayList<>();

        if (!Files.exists(Paths.get(filePath))) {
            log.warn("CSV file not found: {}", filePath);
            return reviews;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                String[] fields = line.split(",");
                if (fields.length >= 6) {
                    try {
                        Review review = new Review();
                        review.setId(Long.parseLong(fields[0]));
                        // userId -> User (solo id)
                        if (fields[1] != null && !fields[1].isBlank()) {
                            User user = new User();
                            user.setId(Long.parseLong(fields[1]));
                            review.setUser(user);
                        }
                        // bookIsbn -> Book (solo isbn)
                        if (fields[2] != null && !fields[2].isBlank()) {
                            Book book = new Book();
                            book.setIsbn(fields[2]);
                            review.setBook(book);
                        }
                        review.setRating(Integer.parseInt(fields[3]));
                        review.setComment(fields[4]);
                        review.setReviewDate(LocalDate.parse(fields[5]));
                        reviews.add(review);
                    } catch (NumberFormatException e) {
                        log.error("Error parsing review line: {}", line, e);
                    }
                }
            }
        }

        log.info("Loaded {} reviews from CSV", reviews.size());
        return reviews;
    }

    public static void writeReviewsToCsv(String filePath, List<Review> reviews) throws IOException {
        // Create directory if it doesn't exist
        Files.createDirectories(Paths.get(filePath).getParent());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header
            writer.write(REVIEWS_CSV_HEADER);
            writer.newLine();

            // Write review data
            for (Review review : reviews) {
                String userIdStr = review.getUser() != null ? Long.toString(review.getUser().getId()) : "";
                String bookIsbnStr = review.getBook() != null ? review.getBook().getIsbn() : "";
                String line = String.format("%d,%s,%s,%d,%s,%s",
                        review.getId(),
                        userIdStr,
                        bookIsbnStr,
                        review.getRating(),
                        escapeCsv(review.getComment()),
                        review.getReviewDate());
                writer.write(line);
                writer.newLine();
            }
        }

        log.info("Wrote {} reviews to CSV", reviews.size());
    }

    // Publishers CSV support
    private static final String PUBLISHERS_CSV_HEADER = "id,name,address,phoneNumber,email";

    public static List<Publisher> readPublishersFromCsv(String filePath) {
        List<Publisher> publishers = new ArrayList<>();
        try {
            if (!Files.exists(Paths.get(filePath))) {
                log.warn("CSV file not found: {}", filePath);
                return publishers;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                boolean isFirstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    String[] fields = line.split(",");
                    if (fields.length >= 5) {
                        try {
                            Publisher p = new Publisher();
                            p.setId(Long.parseLong(fields[0]));
                            p.setName(fields[1]);
                            p.setAddress(fields[2]);
                            p.setPhoneNumber(fields[3]);
                            p.setEmail(fields[4]);
                            publishers.add(p);
                        } catch (Exception ex) {
                            log.error("Error parsing publisher line: {}", line, ex);
                        }
                    }
                }
            }
        } catch (IOException io) {
            log.error("Error reading publishers from CSV: {}", filePath, io);
        }
        log.info("Loaded {} publishers from CSV", publishers.size());
        return publishers;
    }

    public static void writePublishersToCsv(String filePath, List<Publisher> publishers) {
        try {
            Files.createDirectories(Paths.get(filePath).getParent());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(PUBLISHERS_CSV_HEADER);
                writer.newLine();
                if (publishers != null) {
                    for (Publisher p : publishers) {
                        String line = String.format("%d,%s,%s,%s,%s",
                                p.getId(),
                                escapeCsv(p.getName()),
                                escapeCsv(p.getAddress()),
                                escapeCsv(p.getPhoneNumber()),
                                escapeCsv(p.getEmail()));
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }
            log.info("Wrote {} publishers to CSV", publishers != null ? publishers.size() : 0);
        } catch (IOException io) {
            log.error("Error writing publishers to CSV: {}", filePath, io);
        }
    }

    // Returns CSV support
    private static final String RETURNS_CSV_HEADER = "id,loanId,returnDate,condition,notes,fineAmount,finePaid";

    public static List<Return> readReturnsFromCsv(String filePath) {
        List<Return> returns = new ArrayList<>();
        try {
            if (!Files.exists(Paths.get(filePath))) {
                log.warn("CSV file not found: {}", filePath);
                return returns;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                boolean isFirstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    String[] fields = line.split(",");
                    if (fields.length >= 7) {
                        try {
                            Return r = new Return();
                            r.setId(Long.parseLong(fields[0]));
                            // loanId -> Loan (only id)
                            if (fields[1] != null && !fields[1].isBlank()) {
                                Loan loan = new Loan();
                                loan.setId(Long.parseLong(fields[1]));
                                r.setLoan(loan);
                            }
                            r.setReturnDate(fields[2] != null && !fields[2].isBlank() ? LocalDateTime.parse(fields[2]) : null);
                            r.setCondition(parseBookCondition(fields[3]));
                            r.setNotes(fields[4]);
                            r.setFineAmount(fields[5] != null && !fields[5].isBlank() ? Double.parseDouble(fields[5]) : 0.0);
                            r.setFinePaid(fields[6] != null && !fields[6].isBlank() && Boolean.parseBoolean(fields[6]));
                            returns.add(r);
                        } catch (Exception ex) {
                            log.error("Error parsing return line: {}", line, ex);
                        }
                    }
                }
            }
        } catch (IOException io) {
            log.error("Error reading returns from CSV: {}", filePath, io);
        }
        log.info("Loaded {} returns from CSV", returns.size());
        return returns;
    }

    public static void writeReturnsToCsv(String filePath, List<Return> returns) {
        try {
            Files.createDirectories(Paths.get(filePath).getParent());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(RETURNS_CSV_HEADER);
                writer.newLine();
                if (returns != null) {
                    for (Return r : returns) {
                        String loanId = r.getLoan() != null ? String.valueOf(r.getLoan().getId()) : "";
                        String line = String.format("%d,%s,%s,%s,%s,%.2f,%b",
                                r.getId(),
                                loanId,
                                r.getReturnDate() != null ? r.getReturnDate().toString() : "",
                                r.getCondition() != null ? r.getCondition().name() : "",
                                escapeCsv(r.getNotes()),
                                r.getFineAmount(),
                                r.isFinePaid());
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }
            log.info("Wrote {} returns to CSV", returns != null ? returns.size() : 0);
        } catch (IOException io) {
            log.error("Error writing returns to CSV: {}", filePath, io);
        }
    }

    // Generic CSV helpers (minimal stubs to satisfy services depending on generic CSV IO)
    public static <T> List<T> readFromCsv(String filePath, Class<T> clazz) {
        if (!Files.exists(Paths.get(filePath))) {
            log.warn("CSV file not found: {}", filePath);
            return new ArrayList<>();
        }
        log.warn("Generic readFromCsv is not implemented for type: {}. Returning empty list.", clazz != null ? clazz.getSimpleName() : "unknown");
        return new ArrayList<>();
    }

    public static <T> void writeToCsv(String filePath, List<T> data) {
        try {
            Files.createDirectories(Paths.get(filePath).getParent());
        } catch (IOException e) {
            log.error("Error creating directories for CSV path: {}", filePath, e);
        }
        int size = data != null ? data.size() : 0;
        log.warn("Generic writeToCsv is not implemented. Requested write of {} records to {}.", size, filePath);
    }
}
