package net.mixednutz.app.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.post.series.Rating;

@Repository
public interface RatingRepository extends CrudRepository<Rating, String> {

	Iterable<Rating> findByOrderBySortOrderAsc();
	
}
