package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.User;
import co.edu.umanizales.library.util.CsvUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private static final String CSV_FILE_PATH = "data/users.csv";
    private List<User> users;

    public UserService() {
        try {
            this.users = CsvUtil.readUsersFromCsv(CSV_FILE_PATH);
        } catch (IOException e) {
            log.error("Error loading users from CSV", e);
            this.users = List.of();
        }
    }

    public List<User> getAllUsers() {
        return users;
    }

    public Optional<User> getUserById(Long id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    public User createUser(User user) throws IOException {
        Long nextId = users.stream()
                .map(User::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1;
        user.setId(nextId);
        users.add(user);
        CsvUtil.writeUsersToCsv(CSV_FILE_PATH, users);
        log.info("User created with id: {}", nextId);
        return user;
    }

    public Optional<User> updateUser(Long id, User userDetails) throws IOException {
        Optional<User> existingUser = users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setPhoneNumber(userDetails.getPhoneNumber());
            user.setUsername(userDetails.getUsername());
            user.setPassword(userDetails.getPassword());
            user.setRole(userDetails.getRole());
            user.setActive(userDetails.isActive());
            CsvUtil.writeUsersToCsv(CSV_FILE_PATH, users);
            log.info("User updated with id: {}", id);
        }

        return existingUser;
    }

    public boolean deleteUser(Long id) throws IOException {
        boolean removed = users.removeIf(user -> user.getId().equals(id));
        if (removed) {
            CsvUtil.writeUsersToCsv(CSV_FILE_PATH, users);
            log.info("User deleted with id: {}", id);
        }
        return removed;
    }

    public List<User> getUsersByRole(String role) {
        return users.stream()
                .filter(user -> user.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }

    public List<User> getActiveUsers() {
        return users.stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
    }
}
