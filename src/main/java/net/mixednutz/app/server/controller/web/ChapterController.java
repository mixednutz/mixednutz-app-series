package net.mixednutz.app.server.controller.web;

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

import net.mixednutz.app.server.controller.BaseChapterController;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.entity.post.series.SeriesFactory;

@Controller
public class ChapterController extends BaseChapterController {

	@RequestMapping(
			value="/{username}/series/{seriesId}/{seriesTitleKey}/chapter/{id}/{titleKey}", 
			method = {RequestMethod.GET,RequestMethod.HEAD})
	public String getChapter(@PathVariable String username, 
			@PathVariable Long seriesId, @PathVariable String seriesTitleKey, 
			@PathVariable Long id, @PathVariable String titleKey, 
			Authentication auth, Model model) {
		Chapter chapter = get(username, seriesId, seriesTitleKey, id, titleKey);
		getChapter(chapter, auth,model);
		model.addAttribute("views", chapter.getViews().size());
		return "series/chapter/view";
	}
	
	@RequestMapping(value="/series/{seriesId}/chapter/new", method = RequestMethod.POST, params="submit")
	public String saveNew(@ModelAttribute(SeriesFactory.MODEL_ATTRIBUTE) Chapter chapter, 
			@PathVariable Long seriesId,
//			@RequestParam("fgroup_id") Integer friendGroupId, 
			@RequestParam("group_id") Long groupId,
			@RequestParam(value="externalFeedId", required=false) Integer[] externalFeedId,
			@RequestParam(value="tagsString", defaultValue="") String tagsString,
			@RequestParam(value="email_fgroup", defaultValue="false") boolean emailFriendGroup,
			@AuthenticationPrincipal User user, Model model, Errors errors) {
		Series series = loadSeries(seriesId);
		chapter = save(chapter, series, groupId, externalFeedId, 
				tagsString, emailFriendGroup, user);

		return "redirect:"+chapter.getUri();
	}	
	
}
