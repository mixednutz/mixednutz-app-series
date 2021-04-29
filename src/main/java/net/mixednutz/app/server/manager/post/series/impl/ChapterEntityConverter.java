package net.mixednutz.app.server.manager.post.series.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import net.mixednutz.api.core.model.Action;
import net.mixednutz.api.core.model.Link;
import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.api.core.model.TagCount;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement.Type;
import net.mixednutz.app.server.entity.Oembeds.Oembed;
import net.mixednutz.app.server.entity.Oembeds.OembedLink;
import net.mixednutz.app.server.entity.TagScore;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.io.manager.PhotoUploadManager.Size;
import net.mixednutz.app.server.manager.ApiElementConverter;
import net.mixednutz.app.server.manager.TagManager;
import net.mixednutz.app.server.repository.ChapterRepository;
import net.mixednutz.app.server.repository.UserRepository;

@Component
public class ChapterEntityConverter implements ApiElementConverter<Chapter>{
	
	private static final Pattern CHAPTER_PATTERN_REST=Pattern.compile(
			"^\\/(?<username>.*)\\/series\\/(?<seriesid>[0-9]*)\\/(?<seriesTitleKey>.*)\\/chapter\\/(?<id>[0-9]*)\\/(?<titleKey>.*)", 
			Pattern.CASE_INSENSITIVE);
		
	@Autowired
	private NetworkInfo networkInfo;
	
	@Autowired
	private ChapterRepository chapterRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private TagManager tagManager;
	
	@Autowired
    private MessageSource messageSource;
	
	private MessageSourceAccessor accessor;
	
	@PostConstruct
    private void init() {
        accessor = new MessageSourceAccessor(messageSource, Locale.ENGLISH);
    }

	@Override
	public InternalTimelineElement toTimelineElement(
			InternalTimelineElement api, Chapter entity, User viewer, String baseUrl) {
		api.setType(new Type("Chapter",
				networkInfo.getHostName(),
				networkInfo.getId()+"_Chapter"));
		api.setId(entity.getId());
		api.setTitle(entity.getSeries().getTitle()+" - "+entity.getTitle());
		
		if (entity.getSeries().getTags()!=null) {
			setTagCounts(api, tagManager.getTagScores(
					entity.getSeries().getTags(), entity.getAuthor(), viewer));
		}
		
		return api;
	}
	
	protected void setTagCounts(InternalTimelineElement api, Iterable<TagScore> tagScores) {
		List<TagCount> tags = new ArrayList<>();
		for (TagScore tag : tagScores) {
			tags.add(toTagCount(tag, api.getUrl()));
		}
		api.setTags(tags);
	}
	
	protected TagCount toTagCount(TagScore tagScore, String baseUrl) {
		TagCount api = new TagCount();
		api.setName(tagScore.getTag());
		api.setDisplayName(tagScore.getTag());
		api.setCount(tagScore.getScore());
		api.setUserIncluded(tagScore.isUserIncluded());
		api.setToggleAction(new Action(
				new Link(baseUrl+"/tag/toggle?tag="+api.getName()), 
				"tag_"+api.getName(), 
				api.getName()));
		return api;
	}

	@Override
	public boolean canConvert(Class<?> entityClazz) {
		return Chapter.class.isAssignableFrom(entityClazz);
	}

	@Override
	public Oembed toOembed(String path, Integer maxwidth, Integer maxheight, 
			String format, Authentication auth, String baseUrl) {
		Matcher matcher = CHAPTER_PATTERN_REST.matcher(path);
		if (matcher.matches()) {
			String username = matcher.group("username");
			Long id = Long.parseLong(matcher.group("id"));

			Optional<Chapter> chapter = get(username, id);
			if (chapter.isPresent()) {
				return toOembedLink(chapter.get(), baseUrl);
			}
		}
		return null;
	}

	@Override
	public boolean canConvertOembed(String path) {
		Matcher matcher = CHAPTER_PATTERN_REST.matcher(path);
		return matcher.matches();
	}
	
	protected Optional<Chapter> get(String username, 
			Long id) {
		Optional<User> profileUser = userRepository.findByUsername(username);
		
		if (profileUser.isPresent()) {
			return chapterRepository
					.findByOwnerAndId(profileUser.get(), id);
		}
		return Optional.empty();
	}
	
	private OembedLink toOembedLink(Chapter chapter, String baseUrl) {
		OembedLink link = new OembedLink();
		link.setTitle(chapter.getSeries().getTitle()+" - "+chapter.getTitle()+" : "+
				accessor.getMessage("site.title"));
		link.setAuthorName(chapter.getAuthor().getUsername());
		if (chapter.getSeries().getCoverFilename()!=null) {
			link.setThumbnailUrl(baseUrl+chapter.getSeries().getCoverUri()+"?size="+Size.BOOK.getSize());
			link.setThumbnailWidth(250);
			link.setThumbnailHeight(400);
		}
		
		return link;
	}

}
