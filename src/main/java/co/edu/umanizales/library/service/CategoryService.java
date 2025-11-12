package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAllCategories();
    Optional<Category> getCategoryById(long id);
    Optional<Category> getCategoryByName(String name);
    Category createCategory(Category category);
    Category updateCategory(long id, Category category);
    boolean deleteCategory(long id);
    void saveToFile();
    void loadFromFile();
}
