package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.Return;
import java.util.List;

public interface ReturnService {
    List<Return> getAllReturns();
    Return getReturnById(long id);
    Return createReturn(Return returnObj);
    Return updateReturn(long id, Return returnObj);
    boolean deleteReturn(long id);
}
