
package es.nextjourney.vs_nextjourney.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.nextjourney.vs_nextjourney.repository.ReviewRepository;
import es.nextjourney.vs_nextjourney.model.Review;

@Service
public class ReviewService {

	@Autowired
	private ReviewRepository reviewRepository;

	public Review createReview(Review review) {
		return reviewRepository.save(review);
	}

	public Review getReviewById(long id) {
		return reviewRepository.findById(id).orElseThrow();
	}

    public void deleteReview(long id) {
        reviewRepository.deleteById(id);
    }

    public Review modifyReview(Review review) {
        return reviewRepository.save(review);
    }

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    public List<Review> getBetterReviews(int limit){
        List<Review> allReviews = reviewRepository.findAll();

        return allReviews.stream()
                .sorted((r1, r2) -> Double.compare(r2.getRating(), r1.getRating())) 
                .limit(limit)
                .toList();
    }

}

