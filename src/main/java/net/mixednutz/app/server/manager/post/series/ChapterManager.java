package net.mixednutz.app.server.manager.post.series;

import java.util.List;

import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.ChapterComment;
import net.mixednutz.app.server.manager.post.PostManager;

public interface ChapterManager extends PostManager<Chapter, ChapterComment> {
	
	/**
	 * Returns public Series from any user.
	 * @param user
	 * @param viewer
	 * @param pageSize
	 * @param paging
	 * @return
	 */
	List<? extends ITimelineElement> getUserChapter(User user, User viewer,
			int pageSize);
		
	void incrementViewCount(Chapter chapter, User viewer);
	
	long wordCount(Chapter chapter);
	
	long readingTime(Chapter chapter);
	
	boolean isVisible(Chapter chapter, User viewer);

}
