package co.edu.umanizales.library.service;

import co.edu.umanizales.library.model.Review;
import java.io.IOException;
import java.util.List;

public interface ReviewService {
    List<Review> getAllReviews();
    Review getReviewById(Long id);
    Review createReview(Review review) throws IOException;
    Review updateReview(Long id, Review reviewDetails) throws IOException;
    boolean deleteReview(Long id) throws IOException;
    List<Review> getReviewsByUserId(Long userId);
    List<Review> getReviewsByBookId(Long bookId);
    List<Review> getReviewsByRating(int rating);
    double getAverageRatingByBookId(Long bookId);
}
