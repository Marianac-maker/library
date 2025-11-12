package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.Publisher;
import co.edu.umanizales.library.util.CsvUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherService {

    private static final String CSV_FILE_PATH = "data/publishers.csv";
    private List<Publisher> publishers = new ArrayList<>();
    private long nextId = 1;

    public List<Publisher> getAllPublishers() {
        if (publishers.isEmpty()) {
            publishers = CsvUtil.readFromCsv(CSV_FILE_PATH, Publisher.class);
            if (!publishers.isEmpty()) {
                nextId = publishers.stream().mapToLong(Publisher::getId).max().orElse(0) + 1;
            }
        }
        return new ArrayList<>(publishers);
    }

    public Publisher getPublisherById(long id) {
        return getAllPublishers().stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Publisher createPublisher(Publisher publisher) {
        publisher.setId(nextId++);
        publishers.add(publisher);
        saveToCsv();
        return publisher;
    }

    public Publisher updatePublisher(long id, Publisher publisher) {
        Publisher existingPublisher = getPublisherById(id);
        if (existingPublisher != null) {
            publisher.setId(id);
            publishers.replaceAll(p -> p.getId() == id ? publisher : p);
            saveToCsv();
            return publisher;
        }
        return null;
    }

    public boolean deletePublisher(long id) {
        boolean removed = publishers.removeIf(p -> p.getId() == id);
        if (removed) {
            saveToCsv();
        }
        return removed;
    }

    private void saveToCsv() {
        CsvUtil.writeToCsv(CSV_FILE_PATH, publishers);
    }
}
