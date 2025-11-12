package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.Person;
import java.util.List;
import java.util.Optional;

public interface PersonService {
    List<Person> getAllPersons();
    Optional<Person> getPersonById(long id);
    Person createPerson(Person person);
    Person updatePerson(long id, Person person);
    boolean deletePerson(long id);
    void saveToFile();
    void loadFromFile();
}
