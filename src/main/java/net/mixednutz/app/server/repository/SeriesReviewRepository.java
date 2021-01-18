package net.mixednutz.app.server.repository;

import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.post.series.SeriesReview;

@Repository
public interface SeriesReviewRepository extends CommentRepository<SeriesReview> {

}
