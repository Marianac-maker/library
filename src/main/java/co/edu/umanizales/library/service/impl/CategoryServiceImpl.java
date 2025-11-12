package co.edu.umanizales.library.service.impl;

import co.edu.umanizales.library.model.Category;
import co.edu.umanizales.library.service.CategoryService;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final List<Category> categories = new ArrayList<>();
    private final Map<String, Category> nameIndex = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private static final String CSV_FILE = "categories.csv";

    public CategoryServiceImpl() {
        loadFromFile();
    }

    @Override
    public List<Category> getAllCategories() {
        return new ArrayList<>(categories);
    }

    @Override
    public Optional<Category> getCategoryById(long id) {
        return categories.stream()
                .filter(category -> category.getId() == id)
                .findFirst();
    }

    @Override
    public Optional<Category> getCategoryByName(String name) {
        return Optional.ofNullable(nameIndex.get(name.toLowerCase()));
    }

    @Override
    public Category createCategory(Category category) {
        // Validate name uniqueness
        if (nameIndex.containsKey(category.getName().toLowerCase())) {
            throw new IllegalArgumentException("Category with this name already exists");
        }

        // Set ID and creation timestamp
        category.setId(idCounter.getAndIncrement());
        
        // Add to collections
        categories.add(category);
        nameIndex.put(category.getName().toLowerCase(), category);
        
        saveToFile();
        return category;
    }

    @Override
    public Category updateCategory(long id, Category updatedCategory) {
        return getCategoryById(id).map(existingCategory -> {
            // If name is being changed, check for uniqueness
            if (!existingCategory.getName().equalsIgnoreCase(updatedCategory.getName()) && 
                nameIndex.containsKey(updatedCategory.getName().toLowerCase())) {
                throw new IllegalArgumentException("Another category with this name already exists");
            }
            
            // Remove old name from index if changed
            if (!existingCategory.getName().equalsIgnoreCase(updatedCategory.getName())) {
                nameIndex.remove(existingCategory.getName().toLowerCase());
                nameIndex.put(updatedCategory.getName().toLowerCase(), existingCategory);
            }
            
            // Update fields
            existingCategory.setName(updatedCategory.getName());
            existingCategory.setDescription(updatedCategory.getDescription());
            
            saveToFile();
            return existingCategory;
        }).orElse(null);
    }

    @Override
    public boolean deleteCategory(long id) {
        return getCategoryById(id).map(category -> {
            boolean removed = categories.remove(category);
            if (removed) {
                nameIndex.remove(category.getName().toLowerCase());
                saveToFile();
            }
            return removed;
        }).orElse(false);
    }

    @Override
    public void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            // Write header
            writer.println("id,name,description");
            
            // Write data
            for (Category category : categories) {
                writer.println(String.format("%d,%s,%s",
                        category.getId(),
                        escapeCsvField(category.getName()),
                        escapeCsvField(category.getDescription())
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving categories to CSV file", e);
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

            categories.clear();
            nameIndex.clear();
            long maxId = 0;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 2) {
                    try {
                        long id = Long.parseLong(parts[0]);
                        String name = unescapeCsvField(parts[1]);
                        String description = parts.length > 2 ? unescapeCsvField(parts[2]) : "";

                        Category category = new Category(id, name, description);
                        categories.add(category);
                        nameIndex.put(name.toLowerCase(), category);

                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing category: " + e.getMessage());
                    }
                }
            }
            
            idCounter.set(maxId + 1);
        } catch (IOException e) {
            throw new RuntimeException("Error loading categories from CSV file", e);
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
