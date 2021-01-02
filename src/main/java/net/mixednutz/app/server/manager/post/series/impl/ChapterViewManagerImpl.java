package net.mixednutz.app.server.manager.post.series.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.ChapterComment;
import net.mixednutz.app.server.entity.post.series.ChapterView;
import net.mixednutz.app.server.manager.post.impl.PostViewManagerImpl;
import net.mixednutz.app.server.manager.post.series.ChapterViewManager;
import net.mixednutz.app.server.repository.ChapterViewRepository;


@Transactional
@Service
public class ChapterViewManagerImpl extends PostViewManagerImpl<Chapter, ChapterComment, ChapterView>
	implements ChapterViewManager {

	@Override
	protected ChapterView create(Chapter Chapter) {
		ChapterView view = new ChapterView();
		view.setChapter(Chapter);
		return view;
	}
	
	@Autowired
	public void setPostViewRepository(ChapterViewRepository postViewRepository) {
		this.postViewRepository = postViewRepository;
	}

}
