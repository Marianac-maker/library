package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.Review;
import co.edu.umanizales.library.util.CsvUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReviewService {

    private static final String CSV_FILE_PATH = "data/reviews.csv";
    private List<Review> reviews;

    public ReviewService() {
        try {
            this.reviews = CsvUtil.readReviewsFromCsv(CSV_FILE_PATH);
        } catch (IOException e) {
            log.error("Error loading reviews from CSV", e);
            this.reviews = List.of();
        }
    }

    public List<Review> getAllReviews() {
        return reviews;
    }

    public Optional<Review> getReviewById(Long id) {
        return reviews.stream()
                .filter(review -> review.getId().equals(id))
                .findFirst();
    }

    public Review createReview(Review review) throws IOException {
        Long nextId = reviews.stream()
                .map(Review::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1;
        review.setId(nextId);
        reviews.add(review);
        CsvUtil.writeReviewsToCsv(CSV_FILE_PATH, reviews);
        log.info("Review created with id: {}", nextId);
        return review;
    }

    public Optional<Review> updateReview(Long id, Review reviewDetails) throws IOException {
        Optional<Review> existingReview = reviews.stream()
                .filter(review -> review.getId().equals(id))
                .findFirst();

        if (existingReview.isPresent()) {
            Review review = existingReview.get();
            review.setUser(reviewDetails.getUser());
            review.setBook(reviewDetails.getBook());
            review.setRating(reviewDetails.getRating());
            review.setComment(reviewDetails.getComment());
            review.setReviewDate(reviewDetails.getReviewDate());
            CsvUtil.writeReviewsToCsv(CSV_FILE_PATH, reviews);
            log.info("Review updated with id: {}", id);
        }

        return existingReview;
    }

    public boolean deleteReview(Long id) throws IOException {
        boolean removed = reviews.removeIf(review -> review.getId().equals(id));
        if (removed) {
            CsvUtil.writeReviewsToCsv(CSV_FILE_PATH, reviews);
            log.info("Review deleted with id: {}", id);
        }
        return removed;
    }

    public List<Review> getReviewsByUserId(Long userId) {
        return reviews.stream()
                .filter(review -> review.getUser() != null && review.getUser().getId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<Review> getReviewsByBookId(Long bookId) {
        return reviews.stream()
                .filter(review -> review.getBook() != null && review.getBook().getId().equals(bookId))
                .collect(Collectors.toList());
    }

    public List<Review> getReviewsByRating(int rating) {
        return reviews.stream()
                .filter(review -> review.getRating() == rating)
                .collect(Collectors.toList());
    }

    public double getAverageRatingByBookId(Long bookId) {
        return reviews.stream()
                .filter(review -> review.getBook() != null && review.getBook().getId().equals(bookId))
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}
