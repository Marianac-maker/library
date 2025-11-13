package co.edu.umanizales.library.service.impl;

import co.edu.umanizales.library.model.Return;
import co.edu.umanizales.library.service.ReturnService;
import co.edu.umanizales.library.util.CsvUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnServiceImpl implements ReturnService {

    private static final String CSV_FILE_PATH = "data/returns.csv";
    private List<Return> returns = new ArrayList<>();
    private long nextId = 1;

    @Override
    public List<Return> getAllReturns() {
        if (returns.isEmpty()) {
            returns = CsvUtil.readReturnsFromCsv(CSV_FILE_PATH);
            if (!returns.isEmpty()) {
                long maxId = 0;
                for (Return r : returns) {
                    if (r.getId() > maxId) {
                        maxId = r.getId();
                    }
                }
                nextId = maxId + 1;
            }
        }
        return new ArrayList<>(returns);
    }

    @Override
    public Return getReturnById(long id) {
        List<Return> list = getAllReturns();
        for (Return r : list) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    @Override
    public Return createReturn(Return returnObj) {
        returnObj.setId(nextId++);
        returnObj.setReturnDate(LocalDateTime.now());
        returns.add(returnObj);
        saveToCsv();
        return returnObj;
    }

    @Override
    public Return updateReturn(long id, Return returnObj) {
        Return existingReturn = getReturnById(id);
        if (existingReturn != null) {
            returnObj.setId(id);
            returnObj.setReturnDate(existingReturn.getReturnDate());
            for (int i = 0; i < returns.size(); i++) {
                if (returns.get(i).getId() == id) {
                    returns.set(i, returnObj);
                    break;
                }
            }
            saveToCsv();
            return returnObj;
        }
        return null;
    }

    @Override
    public boolean deleteReturn(long id) {
        boolean removed = false;
        for (int i = 0; i < returns.size(); i++) {
            if (returns.get(i).getId() == id) {
                returns.remove(i);
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
        CsvUtil.writeReturnsToCsv(CSV_FILE_PATH, returns);
    }
}
