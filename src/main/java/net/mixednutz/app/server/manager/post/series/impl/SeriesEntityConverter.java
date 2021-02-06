package net.mixednutz.app.server.manager.post.series.impl;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement.Type;
import net.mixednutz.app.server.entity.Oembeds.OembedLink;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.manager.ApiElementConverter;
import net.mixednutz.app.server.repository.SeriesRepository;
import net.mixednutz.app.server.repository.UserRepository;

@Component
public class SeriesEntityConverter implements ApiElementConverter<Series> {

	private static final Pattern SERIES_PATTERN_REST=Pattern.compile(
			"^\\/(?<username>.*)\\/series\\/(?<id>[0-9]*)\\/(?<subjectKey>.*)", 
			Pattern.CASE_INSENSITIVE);

	@Autowired
	private NetworkInfo networkInfo;
	
	@Autowired
	private SeriesRepository seriesRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public InternalTimelineElement toTimelineElement(
			InternalTimelineElement api, Series entity, User viewer) {
		api.setType(new Type("Series",
				networkInfo.getHostName(),
				networkInfo.getId()+"_Series"));
		api.setId(entity.getId());
		api.setTitle(entity.getTitle());
		return api;
	}

	@Override
	public boolean canConvert(Class<?> entityClazz) {
		return Series.class.isAssignableFrom(entityClazz);
	}

	@Override
	public OembedLink toOembed(String path, Integer maxwidth, Integer maxheight, String format, Authentication auth) {
		Matcher matcher = SERIES_PATTERN_REST.matcher(path);
		if (matcher.matches()) {
			String username = matcher.group("username");
			Long id = Long.parseLong(matcher.group("id"));

			Optional<Series> series = get(username, id);
			if (series.isPresent()) {
				return toOembedRich(series.get());
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
	
	private OembedLink toOembedRich(Series series) {
		OembedLink rich = new OembedLink();
		rich.setTitle(series.getTitle());
		rich.setAuthorName(series.getAuthor().getUsername());

		return rich;
	}

}
