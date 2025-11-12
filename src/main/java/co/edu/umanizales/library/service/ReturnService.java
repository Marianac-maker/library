package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.Return;
import co.edu.umanizales.library.util.CsvUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnService {

    private static final String CSV_FILE_PATH = "data/returns.csv";
    private List<Return> returns = new ArrayList<>();
    private long nextId = 1;

    public List<Return> getAllReturns() {
        if (returns.isEmpty()) {
            returns = CsvUtil.readFromCsv(CSV_FILE_PATH, Return.class);
            if (!returns.isEmpty()) {
                nextId = returns.stream().mapToLong(Return::getId).max().orElse(0) + 1;
            }
        }
        return new ArrayList<>(returns);
    }

    public Return getReturnById(long id) {
        return getAllReturns().stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Return createReturn(Return returnObj) {
        returnObj.setId(nextId++);
        returnObj.setReturnDate(LocalDateTime.now());
        returns.add(returnObj);
        saveToCsv();
        return returnObj;
    }

    public Return updateReturn(long id, Return returnObj) {
        Return existingReturn = getReturnById(id);
        if (existingReturn != null) {
            returnObj.setId(id);
            returnObj.setReturnDate(existingReturn.getReturnDate());
            returns.replaceAll(r -> r.getId() == id ? returnObj : r);
            saveToCsv();
            return returnObj;
        }
        return null;
    }

    public boolean deleteReturn(long id) {
        boolean removed = returns.removeIf(r -> r.getId() == id);
        if (removed) {
            saveToCsv();
        }
        return removed;
    }

    private void saveToCsv() {
        CsvUtil.writeToCsv(CSV_FILE_PATH, returns);
    }
}
