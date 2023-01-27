package net.mixednutz.app.server.manager.post.series.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.ChapterComment;
import net.mixednutz.app.server.manager.post.impl.AbstractCommentManagerImpl;
import net.mixednutz.app.server.manager.post.series.ChapterCommentManager;
import net.mixednutz.app.server.repository.ChapterCommentRepository;

@Transactional
@Service
public class ChapterCommentManagerImpl extends AbstractCommentManagerImpl<ChapterComment>
	implements ChapterCommentManager {
	
	@Autowired
	public void setPostRepository(ChapterCommentRepository chapterCommentRepository) {
		this.commentRepository = chapterCommentRepository;
	}

	@Override
	protected InternalTimelineElement toTimelineElement(ChapterComment comment, User viewer) {
		return apiManager.toTimelineElement(comment, viewer);
	}

}
