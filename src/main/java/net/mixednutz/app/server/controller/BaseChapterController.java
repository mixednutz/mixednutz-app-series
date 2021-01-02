package net.mixednutz.app.server.controller;

import java.util.List;
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
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.ChapterFactory;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.format.HtmlFilter;
import net.mixednutz.app.server.manager.post.series.ChapterManager;
import net.mixednutz.app.server.repository.ChapterRepository;
import net.mixednutz.app.server.repository.SeriesRepository;
import net.mixednutz.app.server.repository.UserProfileRepository;
import net.mixednutz.app.server.repository.UserRepository;

public class BaseChapterController {

	@Autowired
	private ChapterRepository chapterRepository;
	
	@Autowired
	private ChapterManager chapterManager;
	
	@Autowired
	private SeriesRepository seriesRepository;
	
	@Autowired
	private UserProfileRepository profileRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private List<HtmlFilter> htmlFilters;
	
	@Autowired
	protected ChapterFactory chapterFactory;
	
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
		
		model.addAttribute("chapter", chapter);
		User user = auth!=null?(User) auth.getPrincipal():null;
		
		//HTML Filter
		String filteredHtml = chapter.getBody();
		for (HtmlFilter htmlFilter: htmlFilters) {
			filteredHtml = htmlFilter.filter(filteredHtml);
		}
		chapter.setFilteredBody(filteredHtml);
			
		if (user!=null) {
			chapterManager.incrementViewCount(chapter, user);
//			notificationManager.markAsRead(user, journal);
		} 
		
		if (chapter.getOwner()!=null) {
			model.addAttribute("profile", profileRepository.findById(chapter.getOwner().getUserId()).orElse(null));
		}
//		model.addAttribute("authors", accountManager.loadCommentAuthorsById(journal));
		
		//Side bar stuff
		//TODO Show previous and next chapter
		
		//New Comment Form
		chapterFactory.newCommentForm(model);
						
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
			Integer[] externalFeedId, String tagsString, boolean emailFriendGroup, User user) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		chapter.setSeries(series);
		chapter.setAuthor(user);
		chapter.setAuthorId(user.getUserId());
		if (chapter.getTitle()==null || chapter.getTitle().trim().length()==0) {
			chapter.setTitle("(No Title)");
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
		
		chapter = chapterRepository.save(chapter);
		
		return chapter;
	}
	
	@ExceptionHandler(ResourceMovedPermanentlyException.class)
	public String handleException(final ResourceMovedPermanentlyException e) {
	    return "redirect:"+e.getRedirectUri();
	}

	
}
