package net.mixednutz.app.server.controller.api;

import java.util.Collection;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.mixednutz.app.server.controller.BaseChapterController;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.ChapterReaction;
import net.mixednutz.app.server.manager.ReactionManager;
import net.mixednutz.app.server.repository.EmojiRepository;

@Controller
@RequestMapping({"/api","/internal"})
public class ChapterApiController extends BaseChapterController {
	
	@RequestMapping(value="/{username}/series/{seriesId}/{seriesTitleKey}/chapter/{id}/{titleKey}/reaction/toggle", method = RequestMethod.POST)
	public @ResponseBody ChapterReaction apiToggleReaction(
			@PathVariable String username, 
			@PathVariable Long seriesId, @PathVariable String seriesTitleKey, 
			@PathVariable Long id, @PathVariable String titleKey, 
			@RequestParam(value="emojiId") String emojiId,
			@AuthenticationPrincipal final User user) {
		
		final Chapter chapter = get(username, seriesId, seriesTitleKey, id, titleKey);
		
//		CollectionDifference<ChapterReaction> diff= new CollectionDifference<>(chapter.getReactions());
		ChapterReaction reaction =  reactionManager.toggleReaction(emojiId, chapter.getReactions(), chapter.getAuthor(), 
				user, new NewChapterReaction(emojiRepository, chapter, user));
		if (reaction!=null) {
			reaction = reactionRepository.save(reaction);
			chapterRepository.save(chapter);
//			notificationManager.notifyNewReaction(journal, reaction);
		} else {
//			notificationManager.unnotifiyReaction(diff.missing(journal.getReactions()));
			chapterRepository.save(chapter);
		}
		
		return reaction;
	}
	
	@RequestMapping(value="/{username}/series/{seriesId}/{seriesTitleKey}/chapter/{id}/{titleKey}/reaction", method = RequestMethod.POST)
	public @ResponseBody Collection<ChapterReaction> apiNewReaction(
			@PathVariable String username, 
			@PathVariable Long seriesId, @PathVariable String seriesTitleKey, 
			@PathVariable Long id, @PathVariable String titleKey, 
			@RequestParam(value="emojiId") String emojiId,
			@AuthenticationPrincipal final User user) {
		final Chapter chapter = get(username, seriesId, seriesTitleKey, id, titleKey);
		Collection<ChapterReaction> addedReactions = reactionManager.addReaction(emojiId, chapter.getReactions(), chapter.getAuthor(), 
				user, new NewChapterReaction(emojiRepository, chapter, user));
		for (ChapterReaction reaction: addedReactions) {
			reaction = reactionRepository.save(reaction);
//			notificationManager.notifyNewReaction(journal, reaction);
		}
		chapterRepository.save(chapter);
		return addedReactions;
	}
	
	private static class NewChapterReaction implements ReactionManager.NewReactionCallback<ChapterReaction> {
		private final User user;
		private final Chapter chapter;
		private EmojiRepository emojiRepository;
		
		public NewChapterReaction(EmojiRepository emojiRepository, Chapter chapter, User user) {
			super();
			this.emojiRepository = emojiRepository;
			this.chapter = chapter;
			this.user = user;
		}

		@Override
		public ChapterReaction createReaction(String emojiId) {
			ChapterReaction reaction = new ChapterReaction(chapter, emojiId, user);
			reaction.setEmoji(emojiRepository.findById(emojiId).get());
			return reaction;
		}
		
	}

}
