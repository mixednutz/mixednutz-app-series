package net.mixednutz.app.server.repository;

import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.post.series.ChapterComment;

@Repository
public interface ChapterCommentRepository extends CommentRepository<ChapterComment> {

}
