package net.mixednutz.app.server.manager.post.series.impl;

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

import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement.Type;
import net.mixednutz.app.server.entity.Oembeds.Oembed;
import net.mixednutz.app.server.entity.Oembeds.OembedLink;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.manager.ApiElementConverter;
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
    private MessageSource messageSource;
	
	private MessageSourceAccessor accessor;
	
	@PostConstruct
    private void init() {
        accessor = new MessageSourceAccessor(messageSource, Locale.ENGLISH);
    }

	@Override
	public InternalTimelineElement toTimelineElement(
			InternalTimelineElement api, Chapter entity, User viewer) {
		api.setType(new Type("Chapter",
				networkInfo.getHostName(),
				networkInfo.getId()+"_Chapter"));
		api.setId(entity.getId());
		api.setTitle(entity.getSeries().getTitle()+" - "+entity.getTitle());
		return api;
	}

	@Override
	public boolean canConvert(Class<?> entityClazz) {
		return Chapter.class.isAssignableFrom(entityClazz);
	}

	@Override
	public Oembed toOembed(String path, Integer maxwidth, Integer maxheight, String format, Authentication auth) {
		Matcher matcher = CHAPTER_PATTERN_REST.matcher(path);
		if (matcher.matches()) {
			String username = matcher.group("username");
			Long id = Long.parseLong(matcher.group("id"));

			Optional<Chapter> chapter = get(username, id);
			if (chapter.isPresent()) {
				return toOembedLink(chapter.get());
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
	
	private OembedLink toOembedLink(Chapter chapter) {
		OembedLink link = new OembedLink();
		link.setTitle(chapter.getSeries().getTitle()+" - "+chapter.getTitle()+" : "+
				accessor.getMessage("site.title"));
		link.setAuthorName(chapter.getAuthor().getUsername());

		return link;
	}

}
