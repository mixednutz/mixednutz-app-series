package net.mixednutz.app.server.controller;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartFile;

import net.mixednutz.app.server.controller.exception.ForbiddenExceptions.SeriesForbiddenException;
import net.mixednutz.app.server.controller.exception.ResourceMovedPermanentlyException;
import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.ExternalVisibility;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.Visibility;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.ChapterFactory;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.entity.post.series.SeriesFactory;
import net.mixednutz.app.server.entity.post.series.SeriesReview;
import net.mixednutz.app.server.entity.post.series.SeriesTag;
import net.mixednutz.app.server.format.HtmlFilter;
import net.mixednutz.app.server.io.domain.PersistableMultipartFile;
import net.mixednutz.app.server.io.manager.PhotoUploadManager;
import net.mixednutz.app.server.io.manager.PhotoUploadManager.Size;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.NotificationManager;
import net.mixednutz.app.server.manager.TagManager;
import net.mixednutz.app.server.manager.post.VisibilityManager;
import net.mixednutz.app.server.manager.post.PostManager.NotVisibleType;
import net.mixednutz.app.server.manager.post.series.SeriesManager;
import net.mixednutz.app.server.repository.SeriesRepository;
import net.mixednutz.app.server.repository.SeriesReviewRepository;
import net.mixednutz.app.server.repository.UserProfileRepository;
import net.mixednutz.app.server.repository.UserRepository;

public class BaseSeriesController {
	
	@Autowired
	protected SeriesRepository seriesRepository;
	
	@Autowired
	protected SeriesManager seriesManager;
	
	@Autowired
	protected SeriesReviewRepository seriesReviewRepository;
	
//	@Autowired
//	protected SeriesReviewManager commentManager;
	
	@Autowired
	private UserProfileRepository profileRepository;
	
	@Autowired
	protected UserRepository userRepository;
	
	@Autowired
	protected TagManager tagManager;
	
	@Autowired
	protected SeriesFactory seriesFactory;
	
	@Autowired
	protected ChapterFactory chapterFactory;
	
	@Autowired
	protected NotificationManager notificationManager;
	
	@Autowired
	protected PhotoUploadManager photoUploadManager;
	
	@Autowired
	private List<HtmlFilter> htmlFilters;
	
	@Autowired
	protected VisibilityManager visibilityManager;
	
	@Autowired
	protected ApiManager apiManager;
	
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
	
	protected SeriesReview get(Series series, long commentId) {
		return series.getComments()
			.stream()
			.filter(comment->comment.getCommentId().equals(commentId))
			.findAny()
			.orElseThrow(()->new ResourceNotFoundException("Comment not found"));
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
	
	protected void assertVisibility(final Series series, Authentication auth) {
		Optional<NotVisibleType> assertion = auth==null 
				? this.seriesManager.assertVisible(series) 
						: this.seriesManager.assertVisible(series, (User) auth.getPrincipal());
				
		if (assertion.isPresent()) {
			switch (assertion.get()) {
			case NOT_PUBLISHED_YET:
				throw new SeriesForbiddenException(series, assertion.get(), "This series hasn't been published yet.");
			case NOT_IN_EXTERNAL_LIST:
			case NOT_IN_SELECT_FOLLOWERS:
			case PRIVATE:
				throw new SeriesForbiddenException(series, assertion.get(), "User does not have permission to view this series.");
			case NOT_PUBLIC:
				throw new AuthenticationCredentialsNotFoundException("This is not a public series.");
			default:
				break;
			}
		}
	}
	
	protected String getSeries(final Series series, Authentication auth, Model model) {		
		assertVisibility(series, auth);
		
		model.addAttribute("series", series);
		User user = auth!=null?(User) auth.getPrincipal():null;
		
		//externalLists
		model.addAttribute("externalListId", series.getVisibility().getExternalList().stream()
				.map(ExternalVisibility::getProviderListId).collect(Collectors.toList()));
		
		//HTML Filter
		String filteredHtml = series.getDescription();
		for (HtmlFilter htmlFilter: htmlFilters) {
			filteredHtml = htmlFilter.filter(filteredHtml);
		}
		series.setFilteredDescription(filteredHtml);
					
		if (user!=null) {
			seriesManager.incrementViewCount(series, user);
			notificationManager.markAsRead(user, series);
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
			String[] externalListId,
			Long groupId, 
			Long[] externalFeedId, String tagsString, boolean emailFriendGroup, User user) {
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
		
		Visibility v = visibilityManager.parseVisibility(series.getVisibility().getVisibilityType(), 
				user, externalListId, groupId, externalFeedId);
		if (v!=null) {
			series.setVisibility(v);
		}
		
		series = seriesRepository.save(series);
		
		return series;
	}
	
	@Transactional
	protected Series update(Series form, Long id, 
			MultipartFile coverImage, boolean clearCoverImage,
//			Integer friendGroupId, 
			String[] externalListId,
			Long groupId, 
			Long[] externalFeedId, String tagsString, 
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
		
		String coverFilename = null;
		if (coverImage!=null && !coverImage.getOriginalFilename().equals("")) {
			coverFilename = uploadPhoto(user, coverImage);
		}
		
		entity.setTitle(form.getTitle());
		if (form.getTitle()==null || form.getTitle().trim().length()==0) {
			entity.setTitle("(No Title)");
		}
		entity.setTitleKey(form.getTitleKey());
		entity.setDescription(form.getDescription());
		entity.setGenre(form.getGenre());
		entity.getAdditionalGenres().clear();
		entity.getAdditionalGenres().addAll(form.getAdditionalGenres());
		entity.setRating(form.getRating());
		entity.setStatus(form.getStatus());
		if (coverFilename!=null) {
			entity.setCoverFilename(coverFilename);
		} else if (clearCoverImage) {
			entity.setCoverFilename(null);
		}
		
		visibilityManager.updateVisibility(entity.getVisibility(), form.getVisibility().getVisibilityType(),
				user, externalListId, groupId, externalFeedId);
		
		String[] tagArray = tagManager.splitTags(tagsString);
		mergeTags(tagArray, entity);
		
		return seriesRepository.save(entity);
	}
	
	protected void incrementHitCount(Series entity) {
		entity.incrementHitCount();
		seriesRepository.save(entity);
	}
	
	protected void delete(Long id, User user) {
		Series entity = seriesRepository.findById(id).orElseThrow(()->{
			return new ResourceNotFoundException("");
		});
		if (!entity.getAuthor().equals(user)) {
			throw new AccessDeniedException("Series #"+id+" - That's not yours to edit!");
		}
		
		seriesManager.delete(entity);
	}
	
	/**
	 * Adds and Deletes tags from the thread
	 * 
	 * @param tagArray
	 * @param thread
	 */
	protected void mergeTags(String[] tagArray, final Series series) {
		tagManager.mergeTags(tagArray, series.getTags(), 
				(tagString)->new SeriesTag(series, tagString));
	}
	
	protected SeriesReview getComment(Long commentId) {
		return seriesReviewRepository.findById(commentId)
			.map(comment->{
				//HTML Filter
				String filteredHtml = comment.getBody();
				for (HtmlFilter htmlFilter: htmlFilters) {
					filteredHtml = htmlFilter.filter(filteredHtml);
				}
				comment.setFilteredBody(filteredHtml);
				return comment;
			})
			.orElseThrow(new Supplier<ResourceNotFoundException>() {
				@Override
				public ResourceNotFoundException get() {
					throw new ResourceNotFoundException("Review not found");
				}
			});
	}
	
	protected SeriesReview saveComment(SeriesReview form, Series series, User user) {
		form.setSeries(series);
		form.setAuthor(user);
		form.setAuthorId(user.getUserId());
		
		SeriesReview review = seriesReviewRepository.save(form);
		notificationManager.notifyNewComment(series, review);
		
		return review;
	}
	
	protected SeriesReview updateComment(SeriesReview review) {
		return seriesReviewRepository.save(review);
	}
	
	private String uploadPhoto(User user, MultipartFile file) {
		PersistableMultipartFile pFile = new PersistableMultipartFile();
		pFile.setFile(file);
		try {
			return photoUploadManager.uploadFile(user, pFile, Size.BOOK);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@ExceptionHandler(ResourceMovedPermanentlyException.class)
	public String handleException(final ResourceMovedPermanentlyException e) {
	    return "redirect:"+e.getRedirectUri();
	}
		
}
