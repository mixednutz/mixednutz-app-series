package net.mixednutz.app.server.controller.web;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.activitystreams.model.ActivityImpl;
import org.w3c.activitystreams.model.Note;
import org.w3c.activitystreams.model.activity.Create;

import net.mixednutz.api.activitypub.ActivityPubManager;
import net.mixednutz.app.server.controller.BaseChapterController;
import net.mixednutz.app.server.controller.exception.ForbiddenExceptions.ChapterForbiddenException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.ChapterComment;
import net.mixednutz.app.server.entity.post.series.ChapterFactory;
import net.mixednutz.app.server.entity.post.series.Series;

@Controller
public class ChapterController extends BaseChapterController {

	@Autowired
	private ActivityPubManager activityPubManager;
	
	
	//------------
	// View Mappings
	//------------

	@RequestMapping(
			value="/{username}/series/{seriesId}/{seriesTitleKey}/chapter/{id}/{titleKey}", 
			method = {RequestMethod.GET,RequestMethod.HEAD})
	public String getChapter(@PathVariable String username, 
			@PathVariable Long seriesId, @PathVariable String seriesTitleKey, 
			@PathVariable Long id, @PathVariable String titleKey, 
			Authentication auth, Model model) {
		Chapter chapter = get(username, seriesId, seriesTitleKey, id, titleKey);
		getChapter(chapter, auth,model);
		
		incrementHitCount(chapter);
		model.addAttribute("views", chapter.getViews().size());
		
		// Check for previous/next chapter
		List<Chapter> chapters = chapter.getSeries().getChapters();
		int index = chapters.indexOf(chapter);
		model.addAttribute("hasPrev", index>0);
		if (index>0) {
			model.addAttribute("prevUri",chapters.get(index-1).getUri());
			model.addAttribute("prevPublished", chapters.get(index-1).getDatePublished()!=null);
		}
		if (index+1<chapters.size()) {
			model.addAttribute("hasNext", true);
			model.addAttribute("nextUri",chapters.get(index+1).getUri());
			model.addAttribute("nextPublished", chapters.get(index+1).getDatePublished()!=null);
		} else {
			model.addAttribute("hasNext", false);
		}
		
		return "series/chapter/view";
	}
	
	@RequestMapping(
			value=ActivityPubManager.NOTE_URI_PREFIX+"/{username}/series/{seriesId}/{seriesTitleKey}/chapter/{id}/{titleKey}", 
			method = {RequestMethod.GET,RequestMethod.HEAD},
			produces=ActivityImpl.APPLICATION_ACTIVITY_VALUE)
	public @ResponseBody org.w3c.activitystreams.Object getChapterActivityNote(@PathVariable String username, 
			@PathVariable Long seriesId, @PathVariable String seriesTitleKey, 
			@PathVariable Long id, @PathVariable String titleKey,
			Authentication auth) {
		
		Chapter chapter = get(username, seriesId, seriesTitleKey, id, titleKey);
		assertVisibility(chapter, auth);
		
		return activityPubManager.toNote(apiManager.toTimelineElement(chapter, null), 
				chapter.getAuthor().getUsername(),true);
	}
	@RequestMapping(
			value=ActivityPubManager.CREATE_URI_PREFIX+"/{username}/series/{seriesId}/{seriesTitleKey}/chapter/{id}/{titleKey}", 
			method = {RequestMethod.GET,RequestMethod.HEAD},
			produces=ActivityImpl.APPLICATION_ACTIVITY_VALUE)
	public @ResponseBody org.w3c.activitystreams.Object getChapterActivityCreate(@PathVariable String username, 
			@PathVariable Long seriesId, @PathVariable String seriesTitleKey, 
			@PathVariable Long id, @PathVariable String titleKey,
			Authentication auth) {
		
		Chapter chapter = get(username, seriesId, seriesTitleKey, id, titleKey);
		assertVisibility(chapter, auth);
		
		return activityPubManager.toCreateNote(apiManager.toTimelineElement(chapter, null), 
				chapter.getAuthor().getUsername());
	}
	
	@RequestMapping(value=ActivityPubManager.NOTE_URI_PREFIX+"/{username}/series/{seriesId}/{seriesTitleKey}/chapter/{id}/{titleKey}/comment/{commentId}", 
			method = RequestMethod.GET,
			produces=ActivityImpl.APPLICATION_ACTIVITY_VALUE)
	public @ResponseBody org.w3c.activitystreams.Object getChapterCommentActivityNote(@PathVariable String username, 
			@PathVariable Long seriesId, @PathVariable String seriesTitleKey, 
			@PathVariable Long id, @PathVariable String titleKey, 
			@PathVariable long commentId) {
		final Chapter chapter = get(username, seriesId, seriesTitleKey, id, titleKey);
		final ChapterComment comment = get(chapter, commentId);
		return activityPubManager.toNote(apiManager.toTimelineElement(comment, null), 
				comment.getAuthor().getUsername(), true);
	}
	@RequestMapping(value=ActivityPubManager.CREATE_URI_PREFIX+"/{username}/series/{seriesId}/{seriesTitleKey}/chapter/{id}/{titleKey}/comment/{commentId}", 
			method = RequestMethod.GET,
			produces=ActivityImpl.APPLICATION_ACTIVITY_VALUE)
	public @ResponseBody org.w3c.activitystreams.Object getChapterCommentActivityCreate(@PathVariable String username, 
			@PathVariable Long seriesId, @PathVariable String seriesTitleKey, 
			@PathVariable Long id, @PathVariable String titleKey, 
			@PathVariable long commentId) {
		final Chapter chapter = get(username, seriesId, seriesTitleKey, id, titleKey);
		final ChapterComment comment = get(chapter, commentId);
		return activityPubManager.toCreateNote(apiManager.toTimelineElement(comment, null), 
				comment.getAuthor().getUsername());
	}

	//------------
	// Insert Mappings
	//------------
		
	@RequestMapping(value="/series/{seriesId}/chapter/new", method = RequestMethod.POST, params="submit")
	public String saveNew(@ModelAttribute(ChapterFactory.MODEL_ATTRIBUTE) Chapter chapter, 
			@PathVariable Long seriesId,
//			@RequestParam("fgroup_id") Integer friendGroupId, 
			@RequestParam(value="group_id",required=false) Long groupId,
			@RequestParam(value="externalFeedId", required=false) Long[] externalFeedId,
			@RequestParam(value="tagsString", defaultValue="") String tagsString,
			@RequestParam(value="email_fgroup", defaultValue="false") boolean emailFriendGroup,
			@DateTimeFormat(iso=ISO.DATE_TIME) @RequestParam(value="localPublishDate", required=false) LocalDateTime localPublishDate,
			@AuthenticationPrincipal User user, Model model, Errors errors,
			NativeWebRequest request) {
		Series series = loadSeries(seriesId);
		chapter = save(chapter, series, groupId, externalFeedId, 
				tagsString, emailFriendGroup, localPublishDate, user, request);

		return "redirect:"+chapter.getUri();
	}	
	
	//------------
	// Update Mappings
	//------------
		
	@RequestMapping(value="/series/{seriesId}/chapter/{id}/edit", method = RequestMethod.POST, params="submit")
	public String updateModal(@ModelAttribute("chapter") Chapter chapter, 
			@PathVariable Long seriesId, @PathVariable Long id, 
//			@RequestParam("fgroup_id") Integer friendGroupId, 
			@RequestParam(value="group_id",required=false) Integer groupId,
			@RequestParam(value="tagsString", defaultValue="") String tagsString,
			@DateTimeFormat(iso=ISO.DATE_TIME) @RequestParam(value="localPublishDate", required=false) LocalDateTime localPublishDate,
			@AuthenticationPrincipal User user, Model model, Errors errors) {
		
		Chapter savedChapter = update(chapter, seriesId, id, groupId, tagsString, localPublishDate, user);
		
		return "redirect:"+savedChapter.getUri();
	}
	

	//------------
	// Delete Mappings
	//------------
	

	@RequestMapping(value="/series/{seriesId}/chapter/{id}/delete", method = RequestMethod.POST, params="confirm")
	public String deleteModal(@PathVariable Long seriesId, @PathVariable Long id, 
			@AuthenticationPrincipal User user) {
		
		String seriesUri = delete(seriesId, id, user);
		
		return "redirect:"+seriesUri;
	}
	
	//------------
	// Comments Mappings
	//------------
	
	@RequestMapping(value="/{username}/series/{seriesId}/{seriesTitleKey}/chapter/{id}/{titleKey}/comment/new", method = RequestMethod.POST, params="submit")
	public String comment(@ModelAttribute(ChapterFactory.MODEL_ATTRIBUTE_COMMENT) ChapterComment comment, 
			@PathVariable String username, 
			@PathVariable Long seriesId, @PathVariable String seriesTitleKey,
			@PathVariable Long id, @PathVariable String titleKey, 
			@RequestParam(value="externalFeedId", required=false) Integer externalFeedId,
			@AuthenticationPrincipal User user, Model model, Errors errors) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		
		Chapter chapter = get(username, seriesId, seriesTitleKey, id, titleKey);
		comment = saveComment(comment, chapter, user);
				
		return "redirect:"+comment.getUri();
	}
	
	@RequestMapping(value="/{username}/series/{seriesId}/{seriesTitleKey}/chapter/{id}/{titleKey}/comment/{inReplyToId}/reply", method = RequestMethod.POST, params="submit")
	public String commentReply(@ModelAttribute(ChapterFactory.MODEL_ATTRIBUTE_COMMENT) ChapterComment comment, 
			@PathVariable String username, 
			@PathVariable Long seriesId, @PathVariable String seriesTitleKey,
			@PathVariable Long id, @PathVariable String titleKey, 
			@PathVariable Long inReplyToId,
			@RequestParam(value="externalFeedId", required=false) Integer externalFeedId,
			@AuthenticationPrincipal User user, Model model, Errors errors) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		
		Chapter chapter = get(username, seriesId, seriesTitleKey, id, titleKey);
				
		comment.setInReplyTo(getComment(inReplyToId));
		comment = saveComment(comment, chapter, user);
				
		return "redirect:"+comment.getUri();
	}
	
	@RequestMapping(value="/{username}/series/{seriesId}/{seriesTitleKey}/chapter/{id}/{titleKey}/comment/{commentId}", method = RequestMethod.POST, params="submit")
	public String commentEdit(@ModelAttribute(ChapterFactory.MODEL_ATTRIBUTE_COMMENT) ChapterComment comment, 
			@PathVariable String username, 
			@PathVariable Long seriesId, @PathVariable String seriesTitleKey,
			@PathVariable Long id, @PathVariable String titleKey, 
			@PathVariable Long commentId,
			@AuthenticationPrincipal User user, Model model, Errors errors) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		
		Chapter chapter = get(username, seriesId, seriesTitleKey, id, titleKey);
		ChapterComment existingComment = get(chapter, commentId);
		if (!existingComment.getAuthor().equals(user)) {
			throw new AccessDeniedException("Comment #"+commentId+" - That's not yours to edit!");
		}
		
		existingComment.setBody(comment.getBody());
		comment = updateComment(existingComment);
				
		return "redirect:"+comment.getUri();
	}
	
//	@RequestMapping(
//			value=ActivityPubManager.NOTE_URI_PREFIX+"/{username}/series/{seriesId}/{seriesTitleKey}/chapter/{id}/{titleKey}/reply", 
//			method = {RequestMethod.GET,RequestMethod.HEAD},
//			produces=ActivityImpl.APPLICATION_ACTIVITY_VALUE)
//	public void commentActivityCreate(@RequestBody Create create,
//			@PathVariable String username, 
//			@PathVariable Long seriesId, @PathVariable String seriesTitleKey, 
//			@PathVariable Long id, @PathVariable String titleKey, 
//			@RequestParam(value="emojiId") String emojiId,
//			@AuthenticationPrincipal final User user) {
//		
//		Chapter chapter = get(username, seriesId, seriesTitleKey, id, titleKey);
//		
//		ChapterComment comment = new ChapterComment();
//		Note note = (Note)create.getObject();
////		note.getInReplyTo() //this needs to be a number
//		comment.setInReplyTo(getComment(inReplyToId));
//		comment = saveComment(comment, chapter, user);
//		
//	}
	
	@ExceptionHandler(ChapterForbiddenException.class)
	public ModelAndView handleException(final ChapterForbiddenException e) {
		Map<String, Object> model = new HashMap<>();
		model.put("chapter", e.getChapter());
		return new ModelAndView("series/chapter/forbidden",model);
	}
}
