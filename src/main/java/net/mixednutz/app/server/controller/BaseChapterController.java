package net.mixednutz.app.server.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;

import net.mixednutz.app.server.controller.exception.ResourceMovedPermanentlyException;
import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.VisibilityType;
import net.mixednutz.app.server.entity.ExternalFeedContent;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.ChapterComment;
import net.mixednutz.app.server.entity.post.series.ChapterFactory;
import net.mixednutz.app.server.entity.post.series.ScheduledChapter;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.format.HtmlFilter;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.ExternalFeedManager;
import net.mixednutz.app.server.manager.NotificationManager;
import net.mixednutz.app.server.manager.ReactionManager;
import net.mixednutz.app.server.manager.post.series.ChapterManager;
import net.mixednutz.app.server.manager.post.series.SeriesManager;
import net.mixednutz.app.server.repository.ChapterCommentRepository;
import net.mixednutz.app.server.repository.ChapterRepository;
import net.mixednutz.app.server.repository.EmojiRepository;
import net.mixednutz.app.server.repository.ExternalFeedRepository;
import net.mixednutz.app.server.repository.ReactionRepository;
import net.mixednutz.app.server.repository.SeriesRepository;
import net.mixednutz.app.server.repository.UserProfileRepository;
import net.mixednutz.app.server.repository.UserRepository;

public class BaseChapterController {

	@Autowired
	protected ChapterRepository chapterRepository;
	
	@Autowired
	protected ChapterManager chapterManager;
	
	@Autowired
	private SeriesManager seriesManager;
	
	@Autowired
	protected ChapterCommentRepository chapterCommentRepository;
		
	@Autowired
	private SeriesRepository seriesRepository;
	
	@Autowired
	private UserProfileRepository profileRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private List<HtmlFilter> htmlFilters;
	
	@Autowired
	protected ReactionManager reactionManager;
	
	@Autowired
	protected ReactionRepository reactionRepository;
	
	@Autowired
	protected ChapterFactory chapterFactory;
	
	@Autowired
	protected EmojiRepository emojiRepository;
	
	@Autowired
	protected NotificationManager notificationManager;
	
	@Autowired
	private ExternalFeedRepository externalFeedRepository;
	
	@Autowired
	private ExternalFeedManager externalFeedManager;
	
	@Autowired
	private ApiManager apiManager;
	
	protected Chapter get(String username, 
			Long seriesId, String seriesTitleKey, 
			Long id, String titleKey) {
		User profileUser = userRepository.findByUsername(username)
				.orElseThrow(new Supplier<UserNotFoundException>(){
					@Override
					public UserNotFoundException get() {
						throw new UserNotFoundException("User "+username+" not found");
					}});
		
		Chapter chapter = chapterRepository
			.findByOwnerAndId(profileUser, id)
			.orElseThrow(new Supplier<ResourceNotFoundException>() {
				@Override
				public ResourceNotFoundException get() {
					throw new ResourceNotFoundException("Chapter not found");
				}
			});
				
		if (!chapter.getSeries().getId().equals(seriesId)) {
			//If the Series ID is wrong, throw 404
			throw new ResourceNotFoundException("Chapter not found");
		}
		if (!chapter.getTitleKey().equals(titleKey) ||
				!chapter.getSeries().getTitleKey().equals(seriesTitleKey)) {
			//If either of the title keys are changed, send a 301
			throw new ResourceMovedPermanentlyException("Chapter moved",
					chapter.getUri());
		}
		return chapter;
	}
	
	protected String getChapter(final Chapter chapter, Authentication auth, Model model) {		
		
		//TODO Add check to see if this a Draft/Unpublished
		if (auth==null &&
				!VisibilityType.WORLD.equals(chapter.getVisibility().getVisibilityType())) {
			throw new AuthenticationCredentialsNotFoundException("This is not a public chapter.");
		} else if (auth!=null) {
			//TODO
		}
		
		User user = auth!=null?(User) auth.getPrincipal():null;
		
		if (!chapter.getAuthor().equals(user) && chapter.getDatePublished()==null) {
			throw new ResourceNotFoundException("Chapter not found");
		}
		
		model.addAttribute("chapter", chapter);
		
		//HTML Filter
		String filteredHtml = chapter.getBody();
		for (HtmlFilter htmlFilter: htmlFilters) {
			filteredHtml = htmlFilter.filter(filteredHtml);
		}
		chapter.setFilteredBody(filteredHtml);
		
		//Word Count
		chapter.setWordCount(chapterManager.wordCount(chapter));
		//Reading Time
		chapter.setReadingTime(chapterManager.readingTime(chapter));
			
		if (user!=null) {
			chapterManager.incrementViewCount(chapter, user);
			notificationManager.markAsRead(user, chapter);
		} 
		
		model.addAttribute("reactionScores", reactionManager.getReactionScores(chapter.getReactions(), chapter.getAuthor(), user));
		
		if (chapter.getOwner()!=null) {
			model.addAttribute("profile", profileRepository.findById(chapter.getOwner().getUserId()).orElse(null));
		}
		model.addAttribute("authors", chapterManager.loadCommentAuthors(chapter.getComments()));
		
		//Side bar stuff
		model.addAttribute("recentPosts", seriesManager.getUserSeries(
				chapter.getOwner(), user, 5));
		
		//New Comment Form
		chapterFactory.newCommentForm(model);
		
		//Tags String
//		model.addAttribute("tagsString", tagManager.getTagsString(journal.getTags()));
		
		// Publish Date
		if (chapter.getScheduled()!=null) {
			model.addAttribute("localPublishDate", 
					chapter.getScheduled().getPublishDate().toLocalDateTime());
		}		
						
		return "series/chapter/view";
	}
	
	protected Series loadSeries(Long id) {
		return seriesRepository.findById(id)
			.orElseThrow(new Supplier<ResourceNotFoundException>() {
				@Override
				public ResourceNotFoundException get() {
					throw new ResourceNotFoundException("Series not found");
				}
			});
	}
		
	protected Chapter save(Chapter chapter, 
			Series series,
//			Integer friendGroupId, 
			Long groupId, 
			Long[] externalFeedId, String tagsString, boolean emailFriendGroup, 
			LocalDateTime localPublishDate,
			User user, NativeWebRequest request) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		chapter.setSeries(series);
		chapter.setAuthor(user);
		chapter.setAuthorId(user.getUserId());
		if (chapter.getTitle()==null || chapter.getTitle().trim().length()==0) {
			chapter.setTitle("(No Title)");
		}
		
		//Get First Chapter
		Optional<Chapter> inReplyTo = series.getChapters().stream()
			.filter(c->!c.getCrossposts().isEmpty())
			.min(Comparator.comparing(Chapter::getDatePublished));
		
		if (localPublishDate!=null) {
			chapter.setScheduled(new ScheduledChapter());
			chapter.getScheduled()
				.setPublishDate(ZonedDateTime.of(localPublishDate, ZoneId.systemDefault()));
			chapter.getScheduled().setExternalFeedId(externalFeedId);
			chapter.getScheduled().setEmailFriendGroup(emailFriendGroup);
			inReplyTo.ifPresent(c->chapter.getScheduled().setInReplyTo(c));			
			String chapterId = request.getParameter("channelIdAsString");
			if (chapterId!=null) {
				chapter.getScheduled().getExternalFeedData().put("channelIdAsString", chapterId);
			}
			
		} else {
			chapter.setDatePublished(ZonedDateTime.now());
		}
		if (groupId!=null) {
			chapter.setOwnerId(groupId);
			chapter.setOwner(userRepository.findById(groupId)
					.orElseThrow(new Supplier<UserNotFoundException>(){
						@Override
						public UserNotFoundException get() {
							throw new UserNotFoundException("User or Group with ID "+groupId+" not found");
						}}));
		} else {
			chapter.setOwner(user);
			chapter.setOwnerId(user.getUserId());
		}
		
//		journal.parseVisibility(user, friendGroupId, groupId);
		
		Chapter savedchapter = chapterRepository.save(chapter);
		
		//Feed Actions
		if (chapter.getScheduled()==null) {
			InternalTimelineElement exportableEntity = apiManager.toTimelineElement(chapter, null);
			if (externalFeedId!=null) {
				for (Long feedId: externalFeedId) {
					AbstractFeed feed= externalFeedRepository.findById(feedId).get();
					
					// Get in-reply-to crosspost element
					ExternalFeedContent inReplyToCrosspost=null;
					if (inReplyTo.isPresent()) {
						Optional<ExternalFeedContent> first = inReplyTo.get().getCrossposts().stream()
								.filter(cp->cp.getFeed().equals(feed)).findFirst();
						if (first.isPresent()) {
							inReplyToCrosspost = first.get();
						}
					}					
					
					externalFeedManager.crosspost(feed, 
							exportableEntity.getTitle(), 
							exportableEntity.getUrl(), 
							null, inReplyToCrosspost, (HttpServletRequest) request.getNativeRequest())
					.ifPresent(crosspost->{
						if (savedchapter.getCrossposts()==null) {
							savedchapter.setCrossposts(new HashSet<>());
						}
						savedchapter.getCrossposts().add(crosspost);
						chapterRepository.save(savedchapter);
					});
				}
			}
			
			notificationManager.notifyNewAddition(chapter.getSeries(), chapter);
		}
		
		return chapter;
	}
	
	@Transactional
	protected Chapter update(Chapter form, Long seriesId, Long id, 
//			Integer friendGroupId, 
			Integer groupId, String tagsString, 
			LocalDateTime localPublishDate,
			User user) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		Chapter entity = chapterRepository.findByIdAndSeriesId(id, seriesId).orElseThrow(()->{
			return new ResourceNotFoundException("");
		});
		if (!entity.getAuthor().equals(user)) {
			throw new AccessDeniedException("Series #"+id+" - That's not yours to edit!");
		}
		
		entity.setTitle(form.getTitle());
		if (form.getTitle()==null || form.getTitle().trim().length()==0) {
			entity.setTitle("(No Title)");
		}
		if (localPublishDate!=null) {
			if (entity.getScheduled()==null) {
				entity.setScheduled(new ScheduledChapter());
			}
			entity.getScheduled()
				.setPublishDate(ZonedDateTime.of(localPublishDate, ZoneId.systemDefault()));
		} 
		entity.setTitleKey(form.getTitleKey());
		entity.setDescription(form.getDescription());
		entity.setBody(form.getBody());
		entity.setVisibility(form.getVisibility());
		entity.setHasExplictSexualContent(form.getHasExplictSexualContent());
		
//		journal.parseVisibility(user, friendGroupId, groupId);
				
		return chapterRepository.save(entity);
	}
	
	protected void incrementHitCount(Chapter entity) {
		entity.incrementHitCount();
		chapterRepository.save(entity);
	}
	
	protected String delete(Long seriesId, Long id, User user) {
		Chapter entity = chapterRepository.findByIdAndSeriesId(id, seriesId).orElseThrow(()->{
			return new ResourceNotFoundException("");
		});
		if (!entity.getAuthor().equals(user)) {
			throw new AccessDeniedException("Series #"+id+" - That's not yours to edit!");
		}
		
		chapterRepository.delete(entity);
		
		return entity.getSeries().getUri();
	}
	
	protected ChapterComment getComment(Long commentId) {
		return chapterCommentRepository.findById(commentId)
			.orElseThrow(new Supplier<ResourceNotFoundException>() {
				@Override
				public ResourceNotFoundException get() {
					throw new ResourceNotFoundException("Comment not found");
				}
			});
	}
	
	protected ChapterComment saveComment(ChapterComment form, Chapter chapter, User user) {
		form.setChapter(chapter);
		form.setAuthor(user);
		
		ChapterComment comment = chapterCommentRepository.save(form);
		notificationManager.notifyNewComment(chapter, comment);
		
		return comment;
	}
	
	@ExceptionHandler(ResourceMovedPermanentlyException.class)
	public String handleException(final ResourceMovedPermanentlyException e) {
	    return "redirect:"+e.getRedirectUri();
	}

	
}
