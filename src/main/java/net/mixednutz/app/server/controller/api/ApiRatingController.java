package net.mixednutz.app.server.controller.api;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import net.mixednutz.api.core.model.ApiList;
import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Rating;
import net.mixednutz.app.server.repository.RatingRepository;

@Controller
@RequestMapping({"/internal/rating"})
public class ApiRatingController {
	
	@Autowired
	private RatingRepository ratingRepository;
	
	@RequestMapping(value="", method = RequestMethod.GET)
	public @ResponseBody RatingList getRatings() {
		RatingList list = new RatingList();
		for (Rating rating: ratingRepository.findByOrderBySortOrderAsc()) {
			list.add(rating);
		}
		return list;
	}
	
	@RolesAllowed("ROLE_ADMIN")
	@RequestMapping(value="", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody Rating addRating(
			@RequestBody Rating rating, 
			@AuthenticationPrincipal final User currentUser) {
		return ratingRepository.save(rating);
	}
	
	@RolesAllowed("ROLE_ADMIN")
	@RequestMapping(value="/{id}", method = RequestMethod.PUT)
	public @ResponseBody Rating editRating(
			@PathVariable String id,
			@RequestBody Rating rating, 
			@AuthenticationPrincipal final User currentUser) {
		Rating existing = ratingRepository.findById(id)
			.orElseThrow(()->{
				return new ResourceNotFoundException();
			});
		
		existing.setId(existing.getId());
		existing.setSortOrder(rating.getSortOrder());
		existing.setDisplayName(rating.getDisplayName());
		existing.setDescription(rating.getDescription());
		existing.setRequiresAgeVerification(rating.isRequiresAgeVerification());
		existing.setMinimumAge(rating.getMinimumAge());
		return ratingRepository.save(existing);
	}
	
	public static class RatingList extends ApiList<Rating> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2314764021162994219L;

		public RatingList() {
			super("ratings");
		}
		
	}

}
