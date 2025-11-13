package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(long id);
    Category getCategoryByName(String name);
    Category createCategory(Category category);
    Category updateCategory(long id, Category category);
    boolean deleteCategory(long id);
    void saveToFile();
    void loadFromFile();
}
