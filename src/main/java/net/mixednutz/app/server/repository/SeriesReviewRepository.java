package net.mixednutz.app.server.repository;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.post.series.SeriesReview;

@Repository
public interface SeriesReviewRepository extends CommentRepository<SeriesReview> {
	
	@Query("select c from #{#entityName} c"
			+" left join c.series p"
			+" left join p.visibility.selectFollowers vsf"
			+ " where c.authorId = :authorId"+
			  " and (c.authorId = :viewerId"
			  + " or p.visibility.visibilityType = 'WORLD'"
			  + " or (p.visibility.visibilityType = 'ALL_USERS' and :viewerId is not null)"
			  + " or (p.visibility.visibilityType = 'SELECT_FOLLOWERS' and vsf.userId = :viewerId))"
			  + " and c.dateCreated > :dateCreated")
	List<SeriesReview> queryAuthorPostsByDateCreatedGreaterThan(
			Long authorId, 
			Long viewerId, 
			ZonedDateTime dateCreated, Pageable pageRequest);
	
	@Query("select c from #{#entityName} c"
			+" left join c.series p"
			+" left join p.visibility.selectFollowers vsf"
			+ " where c.authorId = :authorId"+
			  " and (c.authorId = :viewerId"
			  + " or p.visibility.visibilityType = 'WORLD'"
			  + " or (p.visibility.visibilityType = 'ALL_USERS' and :viewerId is not null)"
			  + " or (p.visibility.visibilityType = 'SELECT_FOLLOWERS' and vsf.userId = :viewerId))"
			  + " and c.dateCreated <= :dateCreated")
	List<SeriesReview> queryAuthorPostsByDateCreatedLessThanEquals(
			@Param("authorId")Long authorId, 
			@Param("viewerId")Long viewerId, 
			@Param("dateCreated")ZonedDateTime dateCreated, Pageable pageRequest);

}
