package net.mixednutz.app.server.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.ChapterComment;
import net.mixednutz.app.server.entity.post.series.Series;

@Repository
public interface ChapterRepository extends PostRepository<Chapter, ChapterComment> {

	Optional<Chapter> findByOwnerAndSeriesAndTitleKey(
			User owner,
			Series series,
			String titleKey
			);
	
}
