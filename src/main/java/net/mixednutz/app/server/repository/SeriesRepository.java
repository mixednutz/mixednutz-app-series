package net.mixednutz.app.server.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.entity.post.series.SeriesReview;

@Repository
public interface SeriesRepository extends PostRepository<Series, SeriesReview>, SeriesCustomRepository,
	ChapterGroupRepository {

	Optional<Series> findByOwnerAndTitleKey(
			User owner,
			String titleKey
			);
	
	Optional<Series> findByCoverFilename(String coverFilename);
	
}
