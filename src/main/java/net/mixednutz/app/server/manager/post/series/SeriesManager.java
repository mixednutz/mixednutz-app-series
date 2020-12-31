package net.mixednutz.app.server.manager.post.series;

import java.util.List;
import java.util.Map;

import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.entity.post.series.SeriesReview;
import net.mixednutz.app.server.manager.post.PostManager;

public interface SeriesManager extends PostManager<Series, SeriesReview> {
	
	/**
	 * Returns public Series from any user.
	 * @param user
	 * @param viewer
	 * @param pageSize
	 * @param paging
	 * @return
	 */
	List<? extends ITimelineElement> getUserSeries(User user, User viewer,
			int pageSize);
	
	/**
	 * Returns Series from the people you subscribe to for a given tag.
	 * 
	 * @param user
	 * @param tags
	 * @param excludeId
	 * @return
	 */
	Map<String, List<? extends ITimelineElement>> getSeriesForTag(User user, 
			String[] tags, Long excludeId);
	
	void incrementViewCount(Series series, User viewer);

}
