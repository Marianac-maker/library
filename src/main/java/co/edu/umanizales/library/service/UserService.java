package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.User;
import java.io.IOException;
import java.util.List;
public interface UserService {
    List<User> getAllUsers();
    User getUserById(Long id);
    User createUser(User user) throws IOException;
    User updateUser(Long id, User userDetails) throws IOException;
    boolean deleteUser(Long id) throws IOException;
    List<User> getUsersByRole(String role);
    List<User> getActiveUsers();
}
