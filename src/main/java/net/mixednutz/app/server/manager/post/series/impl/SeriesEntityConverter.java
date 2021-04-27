package net.mixednutz.app.server.manager.post.series.impl;

import java.util.ArrayList;
import java.util.Comparator;
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
import net.mixednutz.api.core.model.Image;
import net.mixednutz.api.core.model.Link;
import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.api.core.model.ReactionCount;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement.Type;
import net.mixednutz.app.server.entity.Oembeds.Oembed;
import net.mixednutz.app.server.entity.Oembeds.OembedLink;
import net.mixednutz.app.server.entity.ReactionScore;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.manager.ApiElementConverter;
import net.mixednutz.app.server.manager.ReactionManager;
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
	private ReactionManager reactionManager;
	
	@Autowired
    private MessageSource messageSource;
	
	private MessageSourceAccessor accessor;
	
	@PostConstruct
    private void init() {
        accessor = new MessageSourceAccessor(messageSource, Locale.ENGLISH);
    }
	
	@Override
	public InternalTimelineElement toTimelineElement(
			InternalTimelineElement api, Series entity, User viewer, String baseUrl) {
		api.setType(new Type("Series",
				networkInfo.getHostName(),
				networkInfo.getId()+"_Series"));
		api.setId(entity.getId());
		api.setTitle(entity.getTitle());
		if (entity.getCoverFilename()!=null) {
			api.getAdditionalData().put("cover", new Image(
					baseUrl+entity.getCoverUri(), entity.getTitle()+" book cover"));
		}
		if (entity.getChapters()!=null && !entity.getChapters().isEmpty()) {
			//set chapters count
			long sizeOfChapters = entity.getChapters().stream()
				.filter((c)-> c.getDatePublished()!=null)
				.count();
			api.getAdditionalData().put("sizeOfChapters", sizeOfChapters);
			
			//set publish date to latest chapter and get latest chapter title
			entity.getChapters().stream()
				.filter((c)-> c.getDatePublished()!=null)
				.max(Comparator.comparing(Chapter::getDatePublished))
				.ifPresent((c)->{
					api.setPostedOnDate(c.getDatePublished());
					api.setLatestSuburl(baseUrl+c.getUri());
					api.setLatestSubtitle(c.getTitle());
					api.setLatestSubdescription(c.getDescription());
				});
			
			//Roll up Reactions:
			setReactionCounts(api, reactionManager.rollupReactionScores(
					entity.getChapters(), entity.getAuthor(), viewer));
		}
		//set series status
		api.getAdditionalData().put("status", entity.getStatus());
		
		return api;
	}
	protected ReactionCount toReactionCount(ReactionScore reactionScore, String baseUrl) {
		ReactionCount api = new ReactionCount();
		api.setId(reactionScore.getEmoji().getId());
		api.setUnicode(reactionScore.getEmoji().getHtmlCode());
		api.setDescription(reactionScore.getEmoji().getDescription());
		api.setCount(reactionScore.getScore());
		api.setUserIncluded(reactionScore.isUserIncluded());
		api.setToggleAction(new Action(
				new Link(baseUrl+"/reaction/toggle?emojiId="+api.getId()),
				"emoji_"+api.getId(),
				api.getUnicode(), 
				api.getDescription()));
		return api;
	}
	
	protected void setReactionCounts(InternalTimelineElement api, Iterable<ReactionScore> reactionScores) {
		List<ReactionCount> reactions = new ArrayList<>();
		for (ReactionScore reaction : reactionScores) {
			reactions.add(toReactionCount(reaction, api.getUrl()));
		}
		api.setReactions(reactions);
	}

	@Override
	public boolean canConvert(Class<?> entityClazz) {
		return Series.class.isAssignableFrom(entityClazz);
	}

	@Override
	public Oembed toOembed(String path, Integer maxwidth, Integer maxheight, 
			String format, Authentication auth, String baseUrl) {
		Matcher matcher = SERIES_PATTERN_REST.matcher(path);
		if (matcher.matches()) {
			String username = matcher.group("username");
			Long id = Long.parseLong(matcher.group("id"));

			Optional<Series> series = get(username, id);
			if (series.isPresent()) {
				return toOembedLink(series.get(), baseUrl);
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
	
	private OembedLink toOembedLink(Series series, String baseUrl) {
		OembedLink link = new OembedLink();
		link.setTitle(series.getTitle()+" : "+accessor.getMessage("site.title"));
		link.setAuthorName(series.getAuthor().getUsername());
		if (series.getCoverFilename()!=null) {
			//TODO - need height and width to make this work
//			link.setThumbnailUrl(baseUrl+series.getCoverUri());
		}
		
		return link;
	}

}
