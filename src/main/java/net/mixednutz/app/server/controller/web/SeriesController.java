package net.mixednutz.app.server.controller.web;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;

import com.rometools.rome.feed.rss.Channel;

import net.mixednutz.app.server.controller.BaseSeriesController;
import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.ChapterFactory;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.entity.post.series.SeriesFactory;
import net.mixednutz.app.server.entity.post.series.SeriesReview;
import net.mixednutz.app.server.io.domain.FileWrapper;
import net.mixednutz.app.server.io.manager.PhotoUploadManager.Size;
import net.mixednutz.app.server.manager.post.series.impl.SeriesEntityConverter;
import net.mixednutz.app.server.series.SeriesEpubView;

@SessionAttributes(value={"series"})
@Controller
public class SeriesController extends BaseSeriesController {
	
	private static final Logger LOG = LoggerFactory.getLogger(SeriesController.class);
	
	public static final String COVERS_STORAGE_DIR = "/covers-storage";
	private static final String COVERS_STORAGE_MAPPING = COVERS_STORAGE_DIR+"/**";

	
	@Autowired
	private SeriesEpubView seriesEpubsView;
	
	@Autowired
	private SeriesEntityConverter seriesEntityConverter;
	
	@Autowired
	private HttpServletRequest request;
		
	//------------
	// View Mappings
	//------------

	@RequestMapping(value="/{username}/series/{id}/{titleKey}", method = {RequestMethod.GET,RequestMethod.HEAD})
	public String getSeries(@PathVariable String username, 
			@PathVariable Long id, @PathVariable String titleKey, 
			Authentication auth, Model model) {
		Series series = get(username, id, titleKey);
		getSeries(series, auth,model);
		incrementHitCount(series);
		
		model.addAttribute("views", series.getViews().size());
		seriesFactory.addNewPostReferenceData(model);
		
		//Word count:
		series.setWordCount(seriesManager.wordCount(series));
		model.addAttribute("wordCount", series.getWordCount());
		//Reading Time:
		series.setReadingTime(seriesManager.readingTime(series));
		model.addAttribute("readingTime", series.getReadingTime());
				
		return "series/view";
	}
	
	@RequestMapping(value="/embed/{username}/series/{id}/{titleKey}", method = RequestMethod.GET)
	public String getSeriesEmbed(@PathVariable String username, 
			@PathVariable Long id, @PathVariable String titleKey, 
			Authentication auth, Model model) {
		Series series = get(username, id, titleKey);
		model.addAttribute("series", series);
		return "series/embed";
	}
	
	@RequestMapping(value="/{username}/series/{id}/{titleKey}.epub", 
			method = RequestMethod.GET, 
			produces="application/epub+zip")
	public View getSeriesEPub(@PathVariable String username, 
			@PathVariable Long id, @PathVariable String titleKey, 
			Authentication auth, Model model) {
		Series series = get(username, id, titleKey);
		model.addAttribute("series", series);
		return seriesEpubsView;
	}
	
	@RequestMapping(value="/rss/{username}/series/{id}/{titleKey}", 
			method = RequestMethod.GET, 
			produces=MediaType.APPLICATION_RSS_XML_VALUE)
	public @ResponseBody Channel getSeriesRss(@PathVariable String username, 
			@PathVariable Long id, @PathVariable String titleKey) {
		Series series = get(username, id, titleKey);
		
		return toChannel(series);
	}
	
	@RequestMapping(value="/series"+COVERS_STORAGE_MAPPING, method = RequestMethod.GET)
	public ResponseEntity<Resource> getCoverResource(
			HttpServletRequest request,
			@RequestParam(value="size", defaultValue="original") String sizeName,
//			@RequestParam(value="rotate", defaultValue="0") int rotateDegrees,
			@AuthenticationPrincipal User user) {
		
				
		String uri = request.getRequestURI();
		int idx = uri.indexOf(COVERS_STORAGE_DIR);
		int beginIdx = idx+COVERS_STORAGE_DIR.length()+1;
		String filename = uri.substring(beginIdx);
		
		System.out.println(filename);
		
		Optional<Series> seriesAccount = seriesRepository.findByCoverFilename(filename);
		if (!seriesAccount.isPresent()) {
			throw new ResourceNotFoundException("Cover "+filename+" not found");
		}
		
		FileWrapper file;
		Size size = Size.getValue(sizeName);
		try {
			file = photoUploadManager.downloadFile(seriesAccount.get().getAuthor(), filename, size);
		} catch (IOException e) {
			LOG.error("Error reading image", e);
			return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (file==null) {
			return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.NOT_FOUND);
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(file.getContentType()));
		
		Resource resource = new FileSystemResource(file.getFile());
		return new ResponseEntity<>(resource, headers, HttpStatus.OK);
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
			@RequestParam("coverImage") MultipartFile coverImage,
			@RequestParam(name="clearCoverImage", defaultValue="false") boolean clearCoverImage,
//			@RequestParam("fgroup_id") Integer friendGroupId, 
			@RequestParam("group_id") Integer groupId,
			@RequestParam(value="tagsString", defaultValue="") String tagsString,
			@AuthenticationPrincipal User user, Model model, Errors errors) {
		
		Series savedSeries = update(series, id, coverImage, clearCoverImage, 
				groupId, tagsString, user);
		
		return "redirect:"+savedSeries.getUri();
	}
	

	//------------
	// Delete Mappings
	//------------
	

	@RequestMapping(value="/series/{id}/delete", method = RequestMethod.POST, params="confirm")
	public String deleteModal(@PathVariable Long id, 
			@AuthenticationPrincipal User user) {
		
		delete(id, user);
		
		return "redirect:/main";
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
	
	@RequestMapping(value="/{username}/series/{seriesId}/{titleKey}/comment/{inReplyToId}/reply", method = RequestMethod.POST, params="submit")
	public String commentReply(@ModelAttribute(ChapterFactory.MODEL_ATTRIBUTE_COMMENT) SeriesReview review, 
			@PathVariable String username, 
			@PathVariable Long seriesId, @PathVariable String titleKey,
			@PathVariable Long inReplyToId,
			@RequestParam(value="externalFeedId", required=false) Integer externalFeedId,
			@AuthenticationPrincipal User user, Model model, Errors errors) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		
		Series series = get(username, seriesId, titleKey);
		
		review.setInReplyTo(getComment(inReplyToId));
		review = saveComment(review, series, user);
				
		return "redirect:"+review.getUri();
	}
	
	private String getBaseUrl() {
		try {
			URL baseUrl = new URL(
					request.getScheme(), 
					request.getServerName(), 
					request.getServerPort(), 
					"");
			return baseUrl.toExternalForm();
		} catch (MalformedURLException e) {
			throw new RuntimeException("Something's wrong with creating the baseUrl!", e);
		}
	}
	
	protected Channel toChannel(Series series) {	
		return seriesEntityConverter.toRssChannel(series, getBaseUrl());
	}
	
	
}
