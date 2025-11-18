package co.edu.umanizales.library.service.impl;

import co.edu.umanizales.library.model.*;
import co.edu.umanizales.library.service.FineRecordService;
import co.edu.umanizales.library.service.LoanService;
import co.edu.umanizales.library.service.UserService;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class FineRecordServiceImpl implements FineRecordService {
    private final List<FineRecord> fineRecords = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private static final String CSV_FILE = "data/fine_records.csv";

    private final UserService userService;
    private final LoanService loanService;

    public FineRecordServiceImpl(UserService userService, LoanService loanService) {
        this.userService = userService;
        this.loanService = loanService;
        loadFromFile();
    }

    @Override
    public List<FineRecord> getAllFineRecords() {
        return new ArrayList<>(fineRecords);
    }

    @Override
    public FineRecord getFineRecordById(long id) {
        for (FineRecord r : fineRecords) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    @Override
    public List<FineRecord> getFineRecordsByUserId(long userId) {
        List<FineRecord> result = new ArrayList<>();
        for (FineRecord r : fineRecords) {
            if (r.getUser() != null && r.getUser().getId() == userId) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public List<FineRecord> getUnpaidFineRecordsByUserId(long userId) {
        List<FineRecord> result = new ArrayList<>();
        for (FineRecord r : fineRecords) {
            if (r.getUser() != null && r.getUser().getId() == userId && !r.isPaid()) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public FineRecord createFineRecord(FineRecord fineRecord) {
        // Validate required fields
        if (fineRecord.getUser() == null || fineRecord.getLoan() == null) {
            throw new IllegalArgumentException("FineRecord must have user and loan");
        }
        if (fineRecord.getAmount() <= 0) {
            throw new IllegalArgumentException("Fine amount must be greater than 0");
        }
        if (fineRecord.getReason() == null) {
            throw new IllegalArgumentException("Fine reason is required");
        }

        User uOpt = userService.getUserById(fineRecord.getUser().getId());
        if (uOpt == null) {
            throw new IllegalArgumentException("User not found");
        }
        Loan lOpt = loanService.getLoanById(fineRecord.getLoan().getId());
        if (lOpt == null) {
            throw new IllegalArgumentException("Loan not found");
        }

        long newId = idCounter.getAndIncrement();
        fineRecord.setId(newId);
        if (fineRecord.getIssueDate() == null) {
            fineRecord.setIssueDate(LocalDate.now());
        }
        fineRecords.add(fineRecord);
        saveToFile();
        return fineRecord;
    }

    @Override
    public FineRecord updateFineRecord(long id, FineRecord updated) {
        FineRecord existing = getFineRecordById(id);
        if (existing != null) {
            // Update fields
            if (updated.getUser() != null) {
                User uOpt = userService.getUserById(updated.getUser().getId());
                if (uOpt == null) {
                    throw new IllegalArgumentException("User not found");
                }
                existing.setUser(updated.getUser());
            }
            if (updated.getLoan() != null) {
                Loan lOpt = loanService.getLoanById(updated.getLoan().getId());
                if (lOpt == null) {
                    throw new IllegalArgumentException("Loan not found");
                }
                existing.setLoan(updated.getLoan());
            }
            existing.setAmount(updated.getAmount());
            existing.setReason(updated.getReason());
            existing.setIssueDate(updated.getIssueDate());
            existing.setDueDate(updated.getDueDate());
            existing.setPaid(updated.isPaid());
            existing.setPaymentDate(updated.getPaymentDate());
            saveToFile();
            return existing;
        }
        return null;
    }

    @Override
    public boolean deleteFineRecord(long id) {
        boolean removed = false;
        Iterator<FineRecord> it = fineRecords.iterator();
        while (it.hasNext()) {
            FineRecord r = it.next();
            if (r.getId() == id) {
                it.remove();
                removed = true;
                break;
            }
        }
        if (removed) {
            saveToFile();
        }
        return removed;
    }

    @Override
    public boolean markAsPaid(long id) {
        FineRecord r = getFineRecordById(id);
        if (r != null) {
            if (!r.isPaid()) {
                r.setPaid(true);
                r.setPaymentDate(LocalDate.now());
                saveToFile();
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public double calculateTotalFinesByUser(long userId) {
        double total = 0.0;
        for (FineRecord r : fineRecords) {
            if (r.getUser() != null && r.getUser().getId() == userId && !r.isPaid()) {
                total += r.getAmount();
            }
        }
        return total;
    }

    @Override
    public void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            // Header
            writer.println("id,userId,loanId,amount,reason,issueDate,dueDate,paid,paymentDate");
            for (FineRecord r : fineRecords) {
                String userId = r.getUser() != null ? String.valueOf(r.getUser().getId()) : "";
                String loanId = r.getLoan() != null ? String.valueOf(r.getLoan().getId()) : "";
                String line = String.format("%d,%s,%s,%.2f,%s,%s,%s,%b,%s",
                        r.getId(),
                        userId,
                        loanId,
                        r.getAmount(),
                        r.getReason() != null ? r.getReason().name() : "",
                        r.getIssueDate() != null ? r.getIssueDate().toString() : "",
                        r.getDueDate() != null ? r.getDueDate().toString() : "",
                        r.isPaid(),
                        r.getPaymentDate() != null ? r.getPaymentDate().toString() : "");
                writer.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving fine records to CSV file", e);
        }
    }

    @Override
    public void loadFromFile() {
        File file = new File(CSV_FILE);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line == null || !line.startsWith("id")) {
                return;
            }
            fineRecords.clear();
            long maxId = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 9) {
                    try {
                        long id = Long.parseLong(parts[0]);
                        String userIdStr = parts[1];
                        String loanIdStr = parts[2];
                        double amount = parts[3].isEmpty() ? 0.0 : Double.parseDouble(parts[3]);
                        FineReason reason = parts[4].isEmpty() ? null : FineReason.valueOf(parts[4]);
                        LocalDate issueDate = parts[5].isEmpty() ? null : LocalDate.parse(parts[5]);
                        LocalDate dueDate = parts[6].isEmpty() ? null : LocalDate.parse(parts[6]);
                        boolean paid = parts[7].isEmpty() ? false : Boolean.parseBoolean(parts[7]);
                        LocalDate paymentDate = parts.length > 8 && !parts[8].isEmpty() ? LocalDate.parse(parts[8]) : null;

                        User user = null;
                        if (!userIdStr.isEmpty()) {
                            User u = userService.getUserById(Long.parseLong(userIdStr));
                            if (u == null) {
                                throw new IllegalStateException("User not found: " + userIdStr);
                            }
                            user = u;
                        }

                        Loan loan = null;
                        if (!loanIdStr.isEmpty()) {
                            Loan l = loanService.getLoanById(Long.parseLong(loanIdStr));
                            if (l == null) {
                                throw new IllegalStateException("Loan not found: " + loanIdStr);
                            }
                            loan = l;
                        }

                        FineRecord record = new FineRecord(id, user, loan, amount, reason, issueDate, dueDate, paid, paymentDate);
                        fineRecords.add(record);
                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing fine record: " + e.getMessage());
                    }
                }
            }
            idCounter.set(maxId + 1);
        } catch (IOException e) {
            throw new RuntimeException("Error loading fine records from CSV file", e);
        }
    }
}
