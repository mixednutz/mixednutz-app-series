package net.mixednutz.app.server.controller;

import java.util.HashSet;
import java.util.function.Supplier;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;

import net.mixednutz.app.server.controller.exception.ResourceMovedPermanentlyException;
import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.VisibilityType;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.ChapterFactory;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.entity.post.series.SeriesFactory;
import net.mixednutz.app.server.entity.post.series.SeriesReview;
import net.mixednutz.app.server.entity.post.series.SeriesTag;
import net.mixednutz.app.server.manager.TagManager;
import net.mixednutz.app.server.manager.post.series.SeriesManager;
import net.mixednutz.app.server.repository.SeriesRepository;
import net.mixednutz.app.server.repository.SeriesReviewRepository;
import net.mixednutz.app.server.repository.UserProfileRepository;
import net.mixednutz.app.server.repository.UserRepository;

public class BaseSeriesController {
	
	@Autowired
	private SeriesRepository seriesRepository;
	
	@Autowired
	private SeriesManager seriesManager;
	
	@Autowired
	protected SeriesReviewRepository seriesReviewRepository;
	
//	@Autowired
//	protected SeriesReviewManager commentManager;
	
	@Autowired
	private UserProfileRepository profileRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	protected TagManager tagManager;
	
	@Autowired
	protected SeriesFactory seriesFactory;
	
	@Autowired
	protected ChapterFactory chapterFactory;
	
	
	protected Series get(String username, Long id, String titleKey) {
		User profileUser = userRepository.findByUsername(username)
				.orElseThrow(new Supplier<UserNotFoundException>(){
					@Override
					public UserNotFoundException get() {
						throw new UserNotFoundException("User "+username+" not found");
					}});
		
		Series series = seriesRepository
			.findByOwnerAndId(profileUser, id)
			.orElseThrow(new Supplier<ResourceNotFoundException>() {
				@Override
				public ResourceNotFoundException get() {
					throw new ResourceNotFoundException("Series not found");
				}
			});
		if (!series.getTitleKey().equals(titleKey)) {
			throw new ResourceMovedPermanentlyException("Series moved",
					series.getUri());
		}
		return series;
	}
	
	protected Series get(Long id) {
		return seriesRepository.findById(id)
			.orElseThrow(new Supplier<ResourceNotFoundException>() {
				@Override
				public ResourceNotFoundException get() {
					throw new ResourceNotFoundException("Series not found");
				}
			});
	}
	
	protected String getSeries(final Series series, Authentication auth, Model model) {		
		if (auth==null &&
				!VisibilityType.WORLD.equals(series.getVisibility().getVisibilityType())) {
			throw new AuthenticationCredentialsNotFoundException("This is not a public journal.");
		} else if (auth!=null) {
			//TODO
		}
		
		model.addAttribute("series", series);
		User user = auth!=null?(User) auth.getPrincipal():null;
		
		//HTML Filter
		//TODO htmlfilter not implemented here yet
			
		if (user!=null) {
			seriesManager.incrementViewCount(series, user);
//			notificationManager.markAsRead(user, journal);
		} 
		
		model.addAttribute("tagScores", tagManager.getTagScores(series.getTags(), series.getAuthor(), user));
		if (series.getOwner()!=null) {
			model.addAttribute("profile", profileRepository.findById(series.getOwner().getUserId()).orElse(null));
		}
		model.addAttribute("authors", seriesManager.loadCommentAuthors(series.getComments()));
		
		//Side bar stuff
		model.addAttribute("recentPosts", seriesManager.getUserSeries(
				series.getOwner(), user, 5));
		if (!series.getTags().isEmpty()) {
			model.addAttribute("tagPosts", seriesManager.getSeriesForTag(user, 
					tagManager.getTagsArray(series.getTags()), series.getId()));
		}
		
		//New Comment Form
		seriesFactory.newCommentForm(model);
		//New Chapter Form
		Chapter chapter = chapterFactory.newPostForm(model, user);
		chapter.setSeries(series);
		
		//Tags String
		model.addAttribute("tagsString", tagManager.getTagsString(series.getTags()));
								
		return "series/view";
	}
	

	protected Series save(Series series, 
//			Integer friendGroupId, 
			Long groupId, 
			Integer[] externalFeedId, String tagsString, boolean emailFriendGroup, User user) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		
		series.setAuthor(user);
		series.setAuthorId(user.getUserId());
		series.setTags(new HashSet<>());
		if (series.getTitle()==null || series.getTitle().trim().length()==0) {
			series.setTitle("(No Title)");
		}
		String[] tagArray = tagManager.splitTags(tagsString);
		for (String tagString : tagArray) {
			series.getTags().add(new SeriesTag(series, tagString));
		}
		
		if (groupId!=null) {
			series.setOwnerId(groupId);
			series.setOwner(userRepository.findById(groupId)
					.orElseThrow(new Supplier<UserNotFoundException>(){
						@Override
						public UserNotFoundException get() {
							throw new UserNotFoundException("User or Group with ID "+groupId+" not found");
						}}));
		} else {
			series.setOwner(user);
			series.setOwnerId(user.getUserId());
		}
		
//		journal.parseVisibility(user, friendGroupId, groupId);
		
		series = seriesRepository.save(series);
		
		return series;
	}
	
	@Transactional
	protected void update(Series form, Long id, 
//			Integer friendGroupId, 
			Integer groupId, String tagsString, 
			User user) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		Series entity = seriesRepository.findById(id).orElseThrow(()->{
			return new ResourceNotFoundException("");
		});
		if (!entity.getAuthor().equals(user)) {
			throw new AccessDeniedException("Series #"+id+" - That's not yours to edit!");
		}
		
		entity.setTitle(form.getTitle());
		if (form.getTitle()==null || form.getTitle().trim().length()==0) {
			entity.setTitle("(No Title)");
		}
		entity.setTitleKey(form.getTitleKey());
		entity.setDescription(form.getDescription());
		entity.setPublishDate(form.getPublishDate());
		entity.setGenre(form.getGenre());
		entity.getAdditionalGenres().clear();
		entity.getAdditionalGenres().addAll(form.getAdditionalGenres());
		entity.setRating(form.getRating());
		entity.setStatus(form.getStatus());
		
//		journal.parseVisibility(user, friendGroupId, groupId);
		
		String[] tagArray = tagManager.splitTags(tagsString);
		mergeTags(tagArray, entity);
		
		seriesRepository.save(entity);
	}
	
	/**
	 * Adds and Deletes tags from the thread
	 * 
	 * @param tagArray
	 * @param thread
	 */
	protected void mergeTags(String[] tagArray, final Series series) {
		tagManager.mergeTags(tagArray, series.getTags(), new TagManager.NewTagCallback<SeriesTag>(){
			@Override
			public SeriesTag createTag(String tagString) {
				return new SeriesTag(series, tagString);
			}});
	}
	
	protected SeriesReview saveComment(SeriesReview form, Series series, User user) {
		form.setSeries(series);
		form.setAuthor(user);
		
		SeriesReview review = seriesReviewRepository.save(form);
//		notificationManager.notifyNewComment(journal, comment);
		
		return review;
	}
	
	@ExceptionHandler(ResourceMovedPermanentlyException.class)
	public String handleException(final ResourceMovedPermanentlyException e) {
	    return "redirect:"+e.getRedirectUri();
	}

}
