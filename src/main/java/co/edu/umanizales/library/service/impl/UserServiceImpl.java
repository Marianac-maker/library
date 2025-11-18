package co.edu.umanizales.library.service.impl;

import co.edu.umanizales.library.model.User;
import co.edu.umanizales.library.model.UserRole;
import co.edu.umanizales.library.service.UserService;
import co.edu.umanizales.library.util.CsvUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final String CSV_FILE_PATH = "data/users.csv";
    private List<User> users;

    public UserServiceImpl() {
        try {
            this.users = CsvUtil.readUsersFromCsv(CSV_FILE_PATH);
        } catch (IOException e) {
            log.error("Error loading users from CSV", e);
            this.users = new ArrayList<>();
        }
    }

    @Override
    public List<User> getAllUsers() {
        return users;
    }

    @Override
    public User getUserById(Long id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    @Override
    public User createUser(User user) throws IOException {
        // Validate required fields
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (user.getRole() == null) {
            throw new IllegalArgumentException("Role is required");
        }

        long maxId = 0;
        for (User u : users) {
            if (u.getId() > maxId) {
                maxId = u.getId();
            }
        }
        Long nextId = maxId + 1;
        user.setId(nextId);
        users.add(user);
        CsvUtil.writeUsersToCsv(CSV_FILE_PATH, users);
        log.info("User created with id: {}", nextId);
        return user;
    }

    @Override
    public User updateUser(Long id, User userDetails) throws IOException {
        User user = getUserById(id);
        if (user != null) {
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setPhoneNumber(userDetails.getPhoneNumber());
            user.setUsername(userDetails.getUsername());
            user.setPassword(userDetails.getPassword());
            user.setRole(userDetails.getRole());
            user.setActive(userDetails.isActive());
            CsvUtil.writeUsersToCsv(CSV_FILE_PATH, users);
            log.info("User updated with id: {}", id);
            return user;
        }
        return null;
    }

    @Override
    public boolean deleteUser(Long id) throws IOException {
        boolean removed = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == id) {
                users.remove(i);
                removed = true;
                break;
            }
        }
        if (removed) {
            CsvUtil.writeUsersToCsv(CSV_FILE_PATH, users);
            log.info("User deleted with id: {}", id);
        }
        return removed;
    }

    @Override
    public List<User> getUsersByRole(String role) {
        UserRole target = null;
        if (role != null) {
            try {
                target = UserRole.valueOf(role.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                log.warn("Unknown role: {}", role);
                return List.of();
            }
        }
        List<User> result = new ArrayList<>();
        for (User user : users) {
            if (user.getRole() == target) {
                result.add(user);
            }
        }
        return result;
    }

    @Override
    public List<User> getActiveUsers() {
        List<User> result = new ArrayList<>();
        for (User user : users) {
            if (user.isActive()) {
                result.add(user);
            }
        }
        return result;
    }
}
