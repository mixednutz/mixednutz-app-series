package net.mixednutz.app.server.manager.post.series.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.entity.post.series.SeriesReview;
import net.mixednutz.app.server.entity.post.series.SeriesView;
import net.mixednutz.app.server.manager.post.impl.PostViewManagerImpl;
import net.mixednutz.app.server.manager.post.series.SeriesViewManager;
import net.mixednutz.app.server.repository.SeriesViewRepository;

@Transactional
@Service
public class SeriesViewManagerImpl extends PostViewManagerImpl<Series, SeriesReview, SeriesView>
	implements SeriesViewManager {

	@Override
	protected SeriesView create(Series series) {
		SeriesView view = new SeriesView();
		view.setSeries(series);
		return view;
	}
	
	@Autowired
	public void setPostViewRepository(SeriesViewRepository postViewRepository) {
		this.postViewRepository = postViewRepository;
	}

}
