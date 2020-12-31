package net.mixednutz.app.server.manager.post.series.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.entity.post.series.SeriesReview;
import net.mixednutz.app.server.entity.post.series.SeriesView;
import net.mixednutz.app.server.manager.post.impl.PostManagerImpl;
import net.mixednutz.app.server.manager.post.series.SeriesManager;
import net.mixednutz.app.server.manager.post.series.SeriesViewManager;
import net.mixednutz.app.server.repository.SeriesRepository;

@Transactional
@Service
public class SeriesManagerImpl extends PostManagerImpl<Series, SeriesReview, SeriesView>
implements SeriesManager {
	
	@Autowired
	public void setPostRepository(SeriesRepository seriesRepository) {
		this.postRepository = seriesRepository;
	}
	
	public SeriesRepository getSeriesRepository() {
		return (SeriesRepository) postRepository;
	}

	@Autowired
	public void setPostViewManager(SeriesViewManager journalViewManager) {
		this.postViewManager = journalViewManager;
	}

	@Override
	protected InternalTimelineElement toTimelineElement(Series series, User viewer) {
		return apiManager.toTimelineElement(series, viewer);
	}
	
	@Override
	public List<? extends ITimelineElement> getUserSeries(User user, User viewer, int pageSize) {
		return this.getUserTimelineInternal(user, viewer, pageSize);
	}

	@Override
	public Map<String, List<? extends ITimelineElement>> getSeriesForTag(User user, String[] tags, Long excludeId) {
		Map<String, List<? extends ITimelineElement>> seriesMap = new HashMap<>();
		for (String tag: tags) {
			List<Series> seriesForTag = getSeriesRepository().getNutsterzSeriesForTag(user, tag, excludeId);
			if (!seriesForTag.isEmpty()) {
				List<InternalTimelineElement> newSeriesList = new ArrayList<>();
				for (Series series: seriesForTag) {
					newSeriesList.add(toTimelineElement(series, null));
				}
				seriesMap.put(tag, newSeriesList);
			}
		}
		return seriesMap;
	}

}
