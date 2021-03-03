package net.mixednutz.app.server.controller.api;

import java.util.Collection;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.mixednutz.app.server.controller.BaseSeriesController;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.entity.post.series.SeriesTag;

@Controller
@RequestMapping({"/api","/internal"})
public class SeriesApiController extends BaseSeriesController {
	
	
	//------------
	// Tags Mappings
	//------------
	
	@RequestMapping(value="/{username}/series/{id}/{titleKey}/tag/toggle", method = RequestMethod.POST)
	public @ResponseBody SeriesTag toggleTag(
			@PathVariable String username, 
			@PathVariable Long id, @PathVariable String titleKey,
			@RequestParam(value="tag") String tagString,
			@AuthenticationPrincipal final User user) {
		final Series series = get(username, id, titleKey);
		
		SeriesTag tag =  tagManager.toggleTag(tagString, series.getTags(), series.getAuthor(), 
				user, (tagStr)->{
					if (user.equals(series.getAuthor())) {
						return new SeriesTag(series, tagString);
					}
					return new SeriesTag(series, tagString, user);
				});
		seriesRepository.save(series);
		return tag;
	}
	
	@RequestMapping(value="/{username}/series/{id}/{titleKey}/tag", method = RequestMethod.POST)
	public @ResponseBody Collection<SeriesTag> apiNewTag(
			@PathVariable String username, 
			@PathVariable Long id, @PathVariable String titleKey,
			@RequestParam(value="tagsString") String tagsString,
			@AuthenticationPrincipal final User user) {
		final Series series = get(username, id, titleKey);
		
		String[] tagArray = tagManager.splitTags(tagsString);
		Collection<SeriesTag> addedTags = addTags(tagArray, series, user);
		seriesRepository.save(series);
		return addedTags;
	}
	
	/**
	 * Adds tags to the thread that are not already in there
	 * 
	 * @param tagArray
	 * @param thread
	 */
	protected Collection<SeriesTag> addTags(final String[] tagArray, final Series series, 
			final User currentUser) {
		return tagManager.addTags(tagArray, series.getTags(), series.getAuthor(), 
				currentUser, (tagString)->{
					if (currentUser.equals(series.getAuthor())) {
						return new SeriesTag(series, tagString);
					}
					return new SeriesTag(series, tagString, currentUser);
				});
	}

}
