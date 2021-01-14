package net.mixednutz.app.server.manager.post.series.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.ChapterComment;
import net.mixednutz.app.server.entity.post.series.ChapterView;
import net.mixednutz.app.server.format.RemoveTagsHtmlFilter;
import net.mixednutz.app.server.manager.post.impl.PostManagerImpl;
import net.mixednutz.app.server.manager.post.series.ChapterManager;
import net.mixednutz.app.server.manager.post.series.ChapterViewManager;
import net.mixednutz.app.server.repository.ChapterRepository;

@Transactional
@Service
public class ChapterManagerImpl extends PostManagerImpl<Chapter, ChapterComment, ChapterView>
implements ChapterManager {
	
	@Autowired
	public void setPostRepository(ChapterRepository chapterRepository) {
		this.postRepository = chapterRepository;
	}
	
	public ChapterRepository getchapterRepository() {
		return (ChapterRepository) postRepository;
	}

	@Autowired
	public void setPostViewManager(ChapterViewManager journalViewManager) {
		this.postViewManager = journalViewManager;
	}

	@Override
	protected InternalTimelineElement toTimelineElement(Chapter chapter, User viewer) {
		return apiManager.toTimelineElement(chapter, viewer);
	}
	
	@Override
	public List<? extends ITimelineElement> getUserChapter(User user, User viewer, int pageSize) {
		return this.getUserTimelineInternal(user, viewer, pageSize);
	}

	@Override
	public long wordCount(Chapter chapter) {
		RemoveTagsHtmlFilter filter = new RemoveTagsHtmlFilter();
		filter.setReplaceWith(" ");
		String sentence = filter
				.filter(chapter.getBody())
				.replaceAll("[.,!]", "")
				.replaceAll("  ", " ");
		
		StringTokenizer tokens = new StringTokenizer(sentence);
		List<String> words = new ArrayList<>();
		while (tokens.hasMoreElements()) {
			words.add(tokens.nextToken());
		}
		return words.size();
//	    return tokens.countTokens();
	}

}
