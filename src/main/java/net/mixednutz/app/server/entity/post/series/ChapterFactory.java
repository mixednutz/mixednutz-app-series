package net.mixednutz.app.server.entity.post.series;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.NewCommentFactory;
import net.mixednutz.app.server.entity.post.NewPostFactory;

@Component
public class ChapterFactory implements NewPostFactory<Chapter>, NewCommentFactory<ChapterComment> {

	public static final String MODEL_ATTRIBUTE = "newchapter";
	public static final String MODEL_ATTRIBUTE_COMMENT = "newComment";
	
	@Override
	public Chapter newPostForm(Model model, User owner) {
		final Chapter chapter = new Chapter();
		model.addAttribute(MODEL_ATTRIBUTE, chapter);
		chapter.setOwnerId(owner!=null?owner.getUserId():null);
		return chapter;
	}

	@Override
	public ChapterComment newCommentForm(Model model) {
		final ChapterComment comment = new ChapterComment();
		model.addAttribute(MODEL_ATTRIBUTE_COMMENT, comment);
		return comment;
	}
	
}
