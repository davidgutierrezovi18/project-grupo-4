
package es.nextjourney.vs_nextjourney.service;

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

}

