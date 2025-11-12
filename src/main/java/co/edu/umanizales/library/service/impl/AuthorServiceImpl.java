package co.edu.umanizales.library.service.impl;

import co.edu.umanizales.library.model.Author;
import co.edu.umanizales.library.service.AuthorService;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class AuthorServiceImpl implements AuthorService {
    private final List<Author> authors = new ArrayList<>();
    private final Map<Long, Author> authorMap = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private static final String CSV_FILE = "authors.csv";

    public AuthorServiceImpl() {
        loadFromFile();
    }

    @Override
    public List<Author> getAllAuthors() {
        return new ArrayList<>(authors);
    }

    @Override
    public Optional<Author> getAuthorById(long id) {
        return Optional.ofNullable(authorMap.get(id));
    }

    @Override
    public List<Author> searchAuthors(String query) {
        String lowerQuery = query.toLowerCase();
        return authors.stream()
                .filter(author -> author.getName().toLowerCase().contains(lowerQuery) ||
                        (author.getBiography() != null && author.getBiography().toLowerCase().contains(lowerQuery)) ||
                        (author.getNationality() != null && author.getNationality().toLowerCase().contains(lowerQuery)))
                .collect(Collectors.toList());
    }

    @Override
    public Author createAuthor(Author author) {
        // Set ID and creation timestamp
        long newId = idCounter.getAndIncrement();
        author.setId(newId);
        
        // Add to collections
        authors.add(author);
        authorMap.put(newId, author);
        
        saveToFile();
        return author;
    }

    @Override
    public Author updateAuthor(long id, Author updatedAuthor) {
        return getAuthorById(id).map(existingAuthor -> {
            // Update fields
            if (updatedAuthor.getName() != null) {
                existingAuthor.setName(updatedAuthor.getName());
            }
            if (updatedAuthor.getEmail() != null) {
                existingAuthor.setEmail(updatedAuthor.getEmail());
            }
            if (updatedAuthor.getPhoneNumber() != null) {
                existingAuthor.setPhoneNumber(updatedAuthor.getPhoneNumber());
            }
            if (updatedAuthor.getBiography() != null) {
                existingAuthor.setBiography(updatedAuthor.getBiography());
            }
            if (updatedAuthor.getNationality() != null) {
                existingAuthor.setNationality(updatedAuthor.getNationality());
            }
            
            saveToFile();
            return existingAuthor;
        }).orElse(null);
    }

    @Override
    public boolean deleteAuthor(long id) {
        return getAuthorById(id).map(author -> {
            boolean removed = authors.remove(author);
            if (removed) {
                authorMap.remove(id);
                saveToFile();
            }
            return removed;
        }).orElse(false);
    }

    @Override
    public void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            // Write header
            writer.println("id,name,email,phoneNumber,biography,nationality");
            
            // Write data
            for (Author author : authors) {
                writer.println(String.format("%d,%s,%s,%s,%s,%s",
                        author.getId(),
                        escapeCsvField(author.getName()),
                        escapeCsvField(author.getEmail()),
                        escapeCsvField(author.getPhoneNumber()),
                        escapeCsvField(author.getBiography()),
                        escapeCsvField(author.getNationality())
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving authors to CSV file", e);
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

            authors.clear();
            authorMap.clear();
            long maxId = 0;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) {
                    try {
                        long id = Long.parseLong(parts[0]);
                        String name = unescapeCsvField(parts[1]);
                        String email = unescapeCsvField(parts[2]);
                        String phoneNumber = unescapeCsvField(parts[3]);
                        String biography = unescapeCsvField(parts[4]);
                        String nationality = unescapeCsvField(parts[5]);

                        Author author = new Author();
                        author.setId(id);
                        author.setName(name);
                        author.setEmail(email);
                        author.setPhoneNumber(phoneNumber);
                        author.setBiography(biography);
                        author.setNationality(nationality);

                        authors.add(author);
                        authorMap.put(id, author);

                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing author: " + e.getMessage());
                    }
                }
            }
            
            idCounter.set(maxId + 1);
        } catch (IOException e) {
            throw new RuntimeException("Error loading authors from CSV file", e);
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
