package co.edu.umanizales.library.service.impl;

import co.edu.umanizales.library.model.Publisher;
import co.edu.umanizales.library.service.PublisherService;
import co.edu.umanizales.library.util.CsvUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PublisherServiceImpl implements PublisherService {

    private static final String CSV_FILE_PATH = "data/publishers.csv";
    private List<Publisher> publishers = new ArrayList<>();
    private long nextId = 1;

    @Override
    public List<Publisher> getAllPublishers() {
        if (publishers.isEmpty()) {
            publishers = CsvUtil.readPublishersFromCsv(CSV_FILE_PATH);
            if (!publishers.isEmpty()) {
                long maxId = 0;
                for (Publisher p : publishers) {
                    if (p.getId() > maxId) {
                        maxId = p.getId();
                    }
                }
                nextId = maxId + 1;
            }
        }
        return new ArrayList<>(publishers);
    }

    @Override
    public Publisher getPublisherById(long id) {
        List<Publisher> list = getAllPublishers();
        for (Publisher p : list) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    @Override
    public Publisher createPublisher(Publisher publisher) {
        publisher.setId(nextId++);
        publishers.add(publisher);
        saveToCsv();
        return publisher;
    }

    @Override
    public Publisher updatePublisher(long id, Publisher publisher) {
        Publisher existingPublisher = getPublisherById(id);
        if (existingPublisher != null) {
            publisher.setId(id);
            for (int i = 0; i < publishers.size(); i++) {
                if (publishers.get(i).getId() == id) {
                    publishers.set(i, publisher);
                    break;
                }
            }
            saveToCsv();
            return publisher;
        }
        return null;
    }

    @Override
    public boolean deletePublisher(long id) {
        boolean removed = false;
        for (int i = 0; i < publishers.size(); i++) {
            if (publishers.get(i).getId() == id) {
                publishers.remove(i);
                removed = true;
                break;
            }
        }
        if (removed) {
            saveToCsv();
        }
        return removed;
    }

    private void saveToCsv() {
        CsvUtil.writePublishersToCsv(CSV_FILE_PATH, publishers);
    }
}
