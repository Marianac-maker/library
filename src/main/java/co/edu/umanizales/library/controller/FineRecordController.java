package co.edu.umanizales.library.controller;

import co.edu.umanizales.library.model.FineRecord;
import co.edu.umanizales.library.service.FineRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fine-records")
public class FineRecordController {

    private final FineRecordService fineRecordService;

    @Autowired
    public FineRecordController(FineRecordService fineRecordService) {
        this.fineRecordService = fineRecordService;
    }

    @GetMapping
    public ResponseEntity<List<FineRecord>> getAllFineRecords() {
        return new ResponseEntity<>(fineRecordService.getAllFineRecords(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FineRecord> getFineRecordById(@PathVariable long id) {
        return fineRecordService.getFineRecordById(id)
                .map(record -> new ResponseEntity<>(record, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FineRecord>> getFineRecordsByUserId(@PathVariable long userId) {
        return new ResponseEntity<>(fineRecordService.getFineRecordsByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/unpaid")
    public ResponseEntity<List<FineRecord>> getUnpaidFineRecordsByUserId(@PathVariable long userId) {
        return new ResponseEntity<>(fineRecordService.getUnpaidFineRecordsByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/total")
    public ResponseEntity<Double> getTotalUnpaidFinesByUserId(@PathVariable long userId) {
        return new ResponseEntity<>(fineRecordService.calculateTotalFinesByUser(userId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<FineRecord> createFineRecord(@RequestBody FineRecord fineRecord) {
        try {
            FineRecord createdRecord = fineRecordService.createFineRecord(fineRecord);
            return new ResponseEntity<>(createdRecord, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FineRecord> updateFineRecord(@PathVariable long id, @RequestBody FineRecord fineRecord) {
        FineRecord updatedRecord = fineRecordService.updateFineRecord(id, fineRecord);
        if (updatedRecord != null) {
            return new ResponseEntity<>(updatedRecord, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Void> markAsPaid(@PathVariable long id) {
        if (fineRecordService.markAsPaid(id)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFineRecord(@PathVariable long id) {
        if (fineRecordService.deleteFineRecord(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
