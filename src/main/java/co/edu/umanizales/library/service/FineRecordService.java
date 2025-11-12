package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.FineRecord;
import java.util.List;
import java.util.Optional;

public interface FineRecordService {
    List<FineRecord> getAllFineRecords();
    Optional<FineRecord> getFineRecordById(long id);
    List<FineRecord> getFineRecordsByUserId(long userId);
    List<FineRecord> getUnpaidFineRecordsByUserId(long userId);
    FineRecord createFineRecord(FineRecord fineRecord);
    FineRecord updateFineRecord(long id, FineRecord fineRecord);
    boolean deleteFineRecord(long id);
    boolean markAsPaid(long id);
    double calculateTotalFinesByUser(long userId);
    void saveToFile();
    void loadFromFile();
}
