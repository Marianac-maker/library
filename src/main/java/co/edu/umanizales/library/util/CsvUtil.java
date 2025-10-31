package co.edu.umanizales.library.util;

import co.edu.umanizales.library.model.User;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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
                        user.setRole(fields[6]);
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
                String line = String.format("%d,%s,%s,%s,%s,%s,%s,%b",
                        user.getId(),
                        escapeCsv(user.getName()),
                        escapeCsv(user.getEmail()),
                        escapeCsv(user.getPhoneNumber()),
                        escapeCsv(user.getUsername()),
                        escapeCsv(user.getPassword()),
                        escapeCsv(user.getRole()),
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
}
