package net.mixednutz.app.server.controller;

import java.util.HashSet;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;

import net.mixednutz.app.server.controller.exception.ResourceMovedPermanentlyException;
import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.VisibilityType;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.entity.post.series.SeriesFactory;
import net.mixednutz.app.server.entity.post.series.SeriesTag;
import net.mixednutz.app.server.manager.TagManager;
import net.mixednutz.app.server.manager.post.series.SeriesManager;
import net.mixednutz.app.server.repository.SeriesRepository;
import net.mixednutz.app.server.repository.UserProfileRepository;
import net.mixednutz.app.server.repository.UserRepository;

public class BaseSeriesController {
	
	@Autowired
	private SeriesRepository seriesRepository;
	
	@Autowired
	private SeriesManager seriesManager;
	
	@Autowired
	private UserProfileRepository profileRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	protected TagManager tagManager;
	
	@Autowired
	protected SeriesFactory seriesFactory;
	
	
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
					throw new ResourceNotFoundException("Journal not found");
				}
			});
		if (!series.getTitleKey().equals(titleKey)) {
			throw new ResourceMovedPermanentlyException("Journal moved",
					series.getUri());
		}
		return series;
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
//		model.addAttribute("authors", accountManager.loadCommentAuthorsById(journal));
		
		//Side bar stuff
		model.addAttribute("recentPosts", seriesManager.getUserSeries(
				series.getOwner(), user, 5));
		if (!series.getTags().isEmpty()) {
			model.addAttribute("tagPosts", seriesManager.getSeriesForTag(user, 
					tagManager.getTagsArray(series.getTags()), series.getId()));
		}
		
		//New Comment Form
		seriesFactory.newCommentForm(model);
		
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
	
	@ExceptionHandler(ResourceMovedPermanentlyException.class)
	public String handleException(final ResourceMovedPermanentlyException e) {
	    return "redirect:"+e.getRedirectUri();
	}

}
