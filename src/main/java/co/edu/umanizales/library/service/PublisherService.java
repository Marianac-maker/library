package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.Publisher;
import java.util.List;
public interface PublisherService {
    List<Publisher> getAllPublishers();
    Publisher getPublisherById(long id);
    Publisher createPublisher(Publisher publisher);
    Publisher updatePublisher(long id, Publisher publisher);
    boolean deletePublisher(long id);
}
