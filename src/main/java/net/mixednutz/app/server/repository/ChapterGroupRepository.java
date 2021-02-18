package net.mixednutz.app.server.repository;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.mixednutz.app.server.entity.User;

public interface ChapterGroupRepository extends GroupedPostRepository {
	
	@Query("select s"
			+ " from Chapter c join c.series s"
			+ " where c.series.owner = :owner and c.datePublished > :datePublished"
			+ " group by s"
			+ " order by max(c.datePublished) desc")
	<P> List<P> queryByOwnerAndDatePublishedGreaterThanOrderByDatePublishedDesc(
			User owner, ZonedDateTime datePublished, Pageable pageRequest);
	
	@Query("from Series s"
			+ " where s.chapters is empty and s.owner = :owner and s.dateCreated > :dateCreated"
			+ " order by s.dateCreated desc")
	<P> List<P> queryByNoPostsOwnerAndDatePublishedGreaterThanOrderByDateCreatedDesc(
			User owner, ZonedDateTime dateCreated, Pageable pageRequest);
	
	@Query("select s"
			+ " from Chapter c join c.series s"
			+ " where c.series.owner = :owner and c.datePublished <= :datePublished"
			+ " group by s"
			+ " order by max(c.datePublished) desc")
	<P> List<P> queryByOwnerAndDatePublishedLessThanEqualOrderByDatePublishedDesc(
			User owner, ZonedDateTime datePublished, Pageable pageRequest);
	
	@Query("from Series s"
			+ " where s.chapters is empty and s.owner = :owner and s.dateCreated <= :dateCreated"
			+ " order by s.dateCreated desc")
	<P> List<P> queryByNoPostsOwnerAndDatePublishedLessThanEqualOrderByDateCreatedDesc(
			User owner, ZonedDateTime dateCreated, Pageable pageRequest);
	
	@Query("select s"
			+ " from Chapter c join c.series s"
			+ " left join c.visibility.selectFollowers vsf"
			+ " where c.series.ownerId = :ownerId"+
			  " and (c.series.ownerId = :viewerId"
			  + " or c.series.authorId = :viewerId"
			  + " or c.visibility.visibilityType = 'WORLD'"
			  + " or (c.visibility.visibilityType = 'ALL_USERS' and :viewerId is not null)"
			  + " or (c.visibility.visibilityType = 'SELECT_FOLLOWERS' and vsf.userId = :viewerId))"
			  + " and c.datePublished > :datePublished"
			  + " group by s"
				+ " order by max(c.datePublished) desc")
	<P> List<P> queryByUsersGroupedPostsByDatePublishedGreaterThan(
			@Param("ownerId")Long ownerId, 
			@Param("viewerId")Long viewerId, 
			@Param("datePublished")ZonedDateTime datePublished, Pageable pageRequest);
	
	@Query("select s"
			+ " from Chapter c join c.series s"
			+ " left join c.visibility.selectFollowers vsf"
			+ " where c.series.ownerId = :ownerId"+
			  " and (c.series.ownerId = :viewerId"
			  + " or c.series.authorId = :viewerId"
			  + " or c.visibility.visibilityType = 'WORLD'"
			  + " or (c.visibility.visibilityType = 'ALL_USERS' and :viewerId is not null)"
			  + " or (c.visibility.visibilityType = 'SELECT_FOLLOWERS' and vsf.userId = :viewerId))"
			  + " and c.datePublished <= :datePublished"
			  + " group by s"
				+ " order by max(c.datePublished) desc")
	<P> List<P> queryByUsersGroupedPostsByDatePublishedLessThanEquals(
			@Param("ownerId")Long ownerId, 
			@Param("viewerId")Long viewerId, 
			@Param("datePublished")ZonedDateTime datePublished, Pageable pageRequest);
}
