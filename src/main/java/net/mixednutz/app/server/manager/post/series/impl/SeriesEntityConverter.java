package net.mixednutz.app.server.manager.post.series.impl;

import java.util.Comparator;
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
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.manager.ApiElementConverter;
import net.mixednutz.app.server.repository.SeriesRepository;
import net.mixednutz.app.server.repository.UserRepository;

@Component
public class SeriesEntityConverter implements ApiElementConverter<Series> {

	private static final Pattern SERIES_PATTERN_REST=Pattern.compile(
			"^\\/(?<username>.*)\\/series\\/(?<id>[0-9]*)\\/(?<subjectKey>[^\\\\w]*)\\z", 
			Pattern.CASE_INSENSITIVE);

	@Autowired
	private NetworkInfo networkInfo;
	
	@Autowired
	private SeriesRepository seriesRepository;
	
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
			InternalTimelineElement api, Series entity, User viewer) {
		api.setType(new Type("Series",
				networkInfo.getHostName(),
				networkInfo.getId()+"_Series"));
		api.setId(entity.getId());
		api.setTitle(entity.getTitle());
		if (entity.getChapters()!=null && !entity.getChapters().isEmpty()) {
			//set publish date to latest chapter
			entity.getChapters().stream()
				.filter((c)-> c.getDatePublished()!=null)
				.max(Comparator.comparing(Chapter::getDatePublished))
				.ifPresent((c)->api.setPostedOnDate(c.getDatePublished()));
		}
		return api;
	}

	@Override
	public boolean canConvert(Class<?> entityClazz) {
		return Series.class.isAssignableFrom(entityClazz);
	}

	@Override
	public Oembed toOembed(String path, Integer maxwidth, Integer maxheight, String format, Authentication auth) {
		Matcher matcher = SERIES_PATTERN_REST.matcher(path);
		if (matcher.matches()) {
			String username = matcher.group("username");
			Long id = Long.parseLong(matcher.group("id"));

			Optional<Series> series = get(username, id);
			if (series.isPresent()) {
				return toOembedLink(series.get());
			}
		}
		return null;
	}

	@Override
	public boolean canConvertOembed(String path) {
		Matcher matcher = SERIES_PATTERN_REST.matcher(path);
		return matcher.matches();
	}
	
	private Optional<Series> get(String username, Long id) {
		Optional<User> profileUser = userRepository.findByUsername(username);
		
		if (profileUser.isPresent()) {
			return seriesRepository.findByOwnerAndId(profileUser.get(), id);
		}
		return Optional.empty();
	}
	
	private OembedLink toOembedLink(Series series) {
		OembedLink link = new OembedLink();
		link.setTitle(series.getTitle()+" : "+accessor.getMessage("site.title"));
		link.setAuthorName(series.getAuthor().getUsername());

		return link;
	}

}
