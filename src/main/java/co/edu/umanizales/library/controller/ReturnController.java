package co.edu.umanizales.library.controller;

import co.edu.umanizales.library.model.Return;
import co.edu.umanizales.library.service.ReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    @GetMapping
    public ResponseEntity<List<Return>> getAllReturns() {
        return ResponseEntity.ok(returnService.getAllReturns());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Return> getReturnById(@PathVariable long id) {
        Return returnObj = returnService.getReturnById(id);
        if (returnObj != null) {
            return ResponseEntity.ok(returnObj);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Return> createReturn(@RequestBody Return returnObj) {
        Return createdReturn = returnService.createReturn(returnObj);
        return new ResponseEntity<>(createdReturn, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Return> updateReturn(@PathVariable long id, @RequestBody Return returnObj) {
        Return updatedReturn = returnService.updateReturn(id, returnObj);
        if (updatedReturn != null) {
            return ResponseEntity.ok(updatedReturn);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReturn(@PathVariable long id) {
        if (returnService.deleteReturn(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
