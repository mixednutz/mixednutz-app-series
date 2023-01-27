package net.mixednutz.app.server.manager.post.series.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.SeriesReview;
import net.mixednutz.app.server.manager.post.impl.AbstractCommentManagerImpl;
import net.mixednutz.app.server.manager.post.series.SeriesReviewManager;
import net.mixednutz.app.server.repository.SeriesReviewRepository;

@Transactional
@Service
public class SeriesReviewManagerImpl extends AbstractCommentManagerImpl<SeriesReview>
	implements SeriesReviewManager {
	
	@Autowired
	public void setPostRepository(SeriesReviewRepository seriesReviewRepository) {
		this.commentRepository = seriesReviewRepository;
	}

	@Override
	protected InternalTimelineElement toTimelineElement(SeriesReview comment, User viewer) {
		return apiManager.toTimelineElement(comment, viewer);
	}

}
