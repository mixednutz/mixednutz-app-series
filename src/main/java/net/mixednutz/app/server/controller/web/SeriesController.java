package net.mixednutz.app.server.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import net.mixednutz.app.server.controller.BaseSeriesController;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.entity.post.series.SeriesFactory;
import net.mixednutz.app.server.entity.post.series.SeriesReview;
import net.mixednutz.app.server.manager.post.series.ChapterManager;

@SessionAttributes(value={"series"})
@Controller
public class SeriesController extends BaseSeriesController {
	
	@Autowired
	private ChapterManager chapterManager;
	
	//------------
	// View Mappings
	//------------

	@RequestMapping(value="/{username}/series/{id}/{titleKey}", method = {RequestMethod.GET,RequestMethod.HEAD})
	public String getJournal(@PathVariable String username, 
			@PathVariable Long id, @PathVariable String titleKey, 
			Authentication auth, Model model) {
		Series series = get(username, id, titleKey);
		getSeries(series, auth,model);
		
		model.addAttribute("views", series.getViews().size());
		seriesFactory.addNewPostReferenceData(model);
		
		//Chapter word count:
		long totalWords = 0;
		for (Chapter chapter: series.getChapters()) {
			long wc = chapterManager.wordCount(chapter);
			totalWords += wc;
			chapter.setWordCount(wc);
		}
		model.addAttribute("wordCount", totalWords);
				
		return "series/view";
	}
	
	//------------
	// Insert Mappings
	//------------
	
	
	@RequestMapping(value="/series/new", method = RequestMethod.POST, params="submit")
	public String saveNew(@ModelAttribute(SeriesFactory.MODEL_ATTRIBUTE) Series series, 
//			@RequestParam("fgroup_id") Integer friendGroupId, 
			@RequestParam("group_id") Long groupId,
			@RequestParam(value="externalFeedId", required=false) Integer[] externalFeedId,
			@RequestParam(value="tagsString", defaultValue="") String tagsString,
			@RequestParam(value="email_fgroup", defaultValue="false") boolean emailFriendGroup,
			@AuthenticationPrincipal User user, Model model, Errors errors) {
		series = save(series, groupId, externalFeedId, 
				tagsString, emailFriendGroup, user);

		return "redirect:"+series.getUri();
	}	
	
	
	//------------
	// Update Mappings
	//------------
	
	@RequestMapping(value="/series/{id}/edit", method = RequestMethod.POST, params="submit")
	public String updateModal(@ModelAttribute("series") Series series, 
			@PathVariable Long id, 
//			@RequestParam("fgroup_id") Integer friendGroupId, 
			@RequestParam("group_id") Integer groupId,
			@RequestParam(value="tagsString", defaultValue="") String tagsString,
			@AuthenticationPrincipal User user, Model model, Errors errors) {
		
		Series savedSeries = update(series, id, groupId, tagsString, user);
		
		return "redirect:"+savedSeries.getUri();
	}
	
	//------------
	// Comments Mappings
	//------------
	
	@RequestMapping(value="/{username}/series/{seriesId}/{titleKey}/comment/new", method = RequestMethod.POST, params="submit")
	public String comment(@ModelAttribute(SeriesFactory.MODEL_ATTRIBUTE_COMMENT) SeriesReview review, 
			@PathVariable String username, 
			@PathVariable Long seriesId, @PathVariable String titleKey,
			@RequestParam(value="externalFeedId", required=false) Integer externalFeedId,
			@AuthenticationPrincipal User user, Model model, Errors errors) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		
		Series series = get(username, seriesId, titleKey);
		review = saveComment(review, series, user);
				
		return "redirect:"+review.getUri();
	}
}
