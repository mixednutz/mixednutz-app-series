package net.mixednutz.app.server.manager.post.series.impl;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.rometools.rome.feed.rss.Channel;

import net.mixednutz.api.core.model.Action;
import net.mixednutz.api.core.model.Image;
import net.mixednutz.api.core.model.Link;
import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.api.core.model.ReactionCount;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement.Type;
import net.mixednutz.app.server.entity.Oembeds.Oembed;
import net.mixednutz.app.server.entity.Oembeds.OembedRich;
import net.mixednutz.app.server.entity.ReactionScore;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.io.manager.PhotoUploadManager.Size;
import net.mixednutz.app.server.manager.ApiElementConverter;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.ReactionManager;
import net.mixednutz.app.server.manager.post.series.ChapterManager;
import net.mixednutz.app.server.repository.SeriesRepository;
import net.mixednutz.app.server.repository.UserRepository;

@Component
public class SeriesEntityConverter implements ApiElementConverter<Series> {

	private static final Pattern SERIES_PATTERN_REST=Pattern.compile(
			"^\\/(?<username>.*)\\/series\\/(?<id>[0-9]*)\\/(?<subjectKey>[^\\s]*)\\z", 
			Pattern.CASE_INSENSITIVE);
	
	private static final String EMBED_BASE_URL = "/embed";
	
	private static final String TYPE_NAME = "Series";

	@Autowired
	private ChapterEntityConverter chapterEntityConverter;
	
	@Autowired
	private ChapterManager chapterManager;
	
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
	
	@Autowired
	private ApiManager apiManager;
	
	private MessageSourceAccessor accessor;
	
	@PostConstruct
    private void init() {
        accessor = new MessageSourceAccessor(messageSource, Locale.ENGLISH);
    }
	
	@Override
	public InternalTimelineElement toTimelineElement(
			InternalTimelineElement api, Series entity, User viewer, String baseUrl) {
		api.setType(new Type(TYPE_NAME,
				networkInfo.getHostName(),
				networkInfo.getId()+"_"+TYPE_NAME));
		api.setId(entity.getId());
		api.setTitle(entity.getTitle());
		if (entity.getCoverFilename()!=null) {
			api.getAdditionalData().put("cover", new Image(
					baseUrl+entity.getCoverUri()+"?size="+Size.BOOK.getSize(), entity.getTitle()+" book cover"));
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
				.filter(c->viewer==null 
							? this.chapterManager.assertVisible(c).isEmpty() 
							: this.chapterManager.assertVisible(c, viewer).isEmpty())
				.max(Comparator.comparing(Chapter::getDatePublished))
				.ifPresent((c)->{
					api.setPostedOnDate(c.getDatePublished());
					api.setLatestSuburi(c.getUri());
					api.setLatestSuburl(baseUrl+c.getUri());
					api.setLatestSubtitle(c.getTitle());
					api.setLatestSubdescription(c.getDescription());
					api.setVisibility(apiManager.toVisibility(c.getVisibility()));
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
				return toOembedRich(series.get(), baseUrl, maxwidth, maxheight);
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
	
//	private OembedLink toOembedLink(Series series, String baseUrl) {
//		OembedLink link = new OembedLink();
//		link.setTitle(series.getTitle()+" : "+accessor.getMessage("site.title"));
//		link.setAuthorName(series.getAuthor().getUsername());
//		if (series.getCoverFilename()!=null) {
//			link.setThumbnailUrl(baseUrl+series.getCoverUri()+"?size="+Size.BOOK.getSize());
//			link.setThumbnailWidth(250);
//			link.setThumbnailHeight(400);
//		}
//		
//		return link;
//	}
	
	private OembedRich toOembedRich(Series series, String baseUrl,
			int maxwidth, int maxheight) {
		int height = (maxheight > 270 || maxheight <=0) ? 270 : maxheight;
		int width = (maxwidth > 658 || maxwidth <= 0) ? 658 : maxwidth;
		
		OembedRich rich = new OembedRich();
		rich.setTitle(series.getTitle()+" : "+accessor.getMessage("site.title"));
		rich.setAuthorName(series.getAuthor().getUsername());
		rich.setWidth(width);
		rich.setHeight(height);
		StringBuffer html = new StringBuffer();
		html.append("<iframe");
		html.append(" height=\""+height+"\"");
		html.append(" src=\""+networkInfo.getBaseUrl()+EMBED_BASE_URL+series.getUri()+"\"");
		html.append(" style=\"max-width: "+width+"px; width: calc(100% - 2px);\"");
		html.append(" frameborder=\"0\"></iframe>");
		rich.setHtml(html.toString());
		return rich;
	}
	
	public Channel toRssChannel(Series series, String baseUrl) {
		Channel channel = new Channel();
		channel.setFeedType("rss_2.0");
		channel.setTitle(series.getTitle()+" : "+accessor.getMessage("site.title"));
		channel.setDescription(series.getDescription());
		String channelLink = UriComponentsBuilder
				.fromHttpUrl(baseUrl + series.getUri())
				.queryParam("utm_source", "channel")
				.queryParam("utm_medium", "rss")
				.queryParam("utm_campaign", TYPE_NAME.toLowerCase())
				.build().toUriString();
		channel.setLink(channelLink);
		if (series.getCoverFilename()!=null) {
			channel.setImage(new com.rometools.rome.feed.rss.Image());
			channel.getImage().setUrl(baseUrl + series.getCoverUri()+"?size="+Size.BOOK.getSize());
			channel.getImage().setWidth(250);
			channel.getImage().setHeight(400);
			channel.getImage().setTitle(channel.getTitle());
			channel.getImage().setLink(channel.getLink());
		}
		channel.setCopyright("Copyright " + ZonedDateTime.now().getYear() + " " + accessor.getMessage("site.title"));
		channel.setLanguage("en");
		channel.setItems(
				series.getChapters().stream()
					.filter((c)-> c.getDatePublished()!=null)
					.sorted(Comparator.comparing(Chapter::getDatePublished).reversed())
					.map((c)->chapterEntityConverter.toRssItem(c, baseUrl))
					.collect(Collectors.toList()));
		return channel;
	}

}
