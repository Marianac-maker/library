package co.edu.umanizales.library.service.impl;

import co.edu.umanizales.library.model.Person;
import co.edu.umanizales.library.service.PersonService;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PersonServiceImpl implements PersonService {
    private final List<Person> persons = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private static final String CSV_FILE = "persons.csv";

    public PersonServiceImpl() {
        loadFromFile();
    }

    @Override
    public List<Person> getAllPersons() {
        return new ArrayList<>(persons);
    }

    @Override
    public Optional<Person> getPersonById(long id) {
        return persons.stream()
                .filter(person -> person.getId() == id)
                .findFirst();
    }

    @Override
    public Person createPerson(Person person) {
        person.setId(idCounter.getAndIncrement());
        persons.add(person);
        saveToFile();
        return person;
    }

    @Override
    public Person updatePerson(long id, Person person) {
        return getPersonById(id).map(existingPerson -> {
            existingPerson.setName(person.getName());
            existingPerson.setEmail(person.getEmail());
            existingPerson.setPhoneNumber(person.getPhoneNumber());
            saveToFile();
            return existingPerson;
        }).orElse(null);
    }

    @Override
    public boolean deletePerson(long id) {
        boolean removed = persons.removeIf(person -> person.getId() == id);
        if (removed) {
            saveToFile();
        }
        return removed;
    }

    @Override
    public void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            // Write header
            writer.println("id,name,email,phoneNumber");
            
            // Write data
            for (Person person : persons) {
                writer.println(String.format("%d,%s,%s,%s",
                        person.getId(),
                        escapeCsvField(person.getName()),
                        escapeCsvField(person.getEmail()),
                        escapeCsvField(person.getPhoneNumber())));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving persons to CSV file", e);
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

            persons.clear();
            long maxId = 0;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    long id = Long.parseLong(parts[0]);
                    String name = unescapeCsvField(parts[1]);
                    String email = unescapeCsvField(parts[2]);
                    String phoneNumber = unescapeCsvField(parts[3]);
                    
                    persons.add(new Person(id, name, email, phoneNumber));
                    if (id > maxId) {
                        maxId = id;
                    }
                }
            }
            
            idCounter.set(maxId + 1);
        } catch (IOException e) {
            throw new RuntimeException("Error loading persons from CSV file", e);
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
