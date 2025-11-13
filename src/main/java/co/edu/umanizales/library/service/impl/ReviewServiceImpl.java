package co.edu.umanizales.library.service.impl;

import co.edu.umanizales.library.model.Review;
import co.edu.umanizales.library.service.ReviewService;
import co.edu.umanizales.library.util.CsvUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    private static final String CSV_FILE_PATH = "data/reviews.csv";
    private List<Review> reviews;

    public ReviewServiceImpl() {
        try {
            this.reviews = CsvUtil.readReviewsFromCsv(CSV_FILE_PATH);
        } catch (IOException e) {
            log.error("Error loading reviews from CSV", e);
            this.reviews = new ArrayList<>();
        }
    }

    @Override
    public List<Review> getAllReviews() {
        return reviews;
    }

    @Override
    public Review getReviewById(Long id) {
        for (Review review : reviews) {
            if (review.getId() == id) {
                return review;
            }
        }
        return null;
    }

    @Override
    public Review createReview(Review review) throws IOException {
        long maxId = 0;
        for (Review r : reviews) {
            if (r.getId() > maxId) {
                maxId = r.getId();
            }
        }
        Long nextId = maxId + 1;
        review.setId(nextId);
        reviews.add(review);
        CsvUtil.writeReviewsToCsv(CSV_FILE_PATH, reviews);
        log.info("Review created with id: {}", nextId);
        return review;
    }

    @Override
    public Review updateReview(Long id, Review reviewDetails) throws IOException {
        Review review = getReviewById(id);
        if (review != null) {
            review.setUser(reviewDetails.getUser());
            review.setBook(reviewDetails.getBook());
            review.setRating(reviewDetails.getRating());
            review.setComment(reviewDetails.getComment());
            review.setReviewDate(reviewDetails.getReviewDate());
            CsvUtil.writeReviewsToCsv(CSV_FILE_PATH, reviews);
            log.info("Review updated with id: {}", id);
            return review;
        }
        return null;
    }

    @Override
    public boolean deleteReview(Long id) throws IOException {
        boolean removed = false;
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).getId() == id) {
                reviews.remove(i);
                removed = true;
                break;
            }
        }
        if (removed) {
            CsvUtil.writeReviewsToCsv(CSV_FILE_PATH, reviews);
            log.info("Review deleted with id: {}", id);
        }
        return removed;
    }

    @Override
    public List<Review> getReviewsByUserId(Long userId) {
        List<Review> result = new ArrayList<>();
        for (Review r : reviews) {
            if (r.getUser() != null && r.getUser().getId() == userId) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public List<Review> getReviewsByBookId(Long bookId) {
        List<Review> result = new ArrayList<>();
        String target = String.valueOf(bookId);
        for (Review r : reviews) {
            if (r.getBook() != null && r.getBook().getIsbn() != null && r.getBook().getIsbn().equals(target)) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public List<Review> getReviewsByRating(int rating) {
        List<Review> result = new ArrayList<>();
        for (Review r : reviews) {
            if (r.getRating() == rating) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public double getAverageRatingByBookId(Long bookId) {
        String target = String.valueOf(bookId);
        int sum = 0;
        int count = 0;
        for (Review r : reviews) {
            if (r.getBook() != null && r.getBook().getIsbn() != null && r.getBook().getIsbn().equals(target)) {
                sum += r.getRating();
                count++;
            }
        }
        if (count == 0) {
            return 0.0;
        }
        return (double) sum / count;
    }
}
