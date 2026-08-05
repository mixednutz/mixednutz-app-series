package net.mixednutz.app.server.manager.post.series.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import net.mixednutz.app.server.entity.ComponentSettings;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Genre;
import net.mixednutz.app.server.entity.post.series.Rating;
import net.mixednutz.app.server.repository.GenreRepository;
import net.mixednutz.app.server.repository.RatingRepository;
import net.mixednutz.app.server.repository.UserRepository;

@Component
public class SeriesSettingsManager implements ComponentSettings {
	
	@Autowired
	private GenreRepository genreRepository;
	
	@Autowired
	private RatingRepository ratingRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	//TODO tag groups (ie character age
	//TODO permanant tags
	

	public Iterable<Genre> genres() {
		return genreRepository.findByOrderByDisplayNameAsc();
	}
	
	public Iterable<Rating> ratings() {
		return ratingRepository.findByOrderBySortOrderAsc();
	}
	
	/**
	 * this is used to build a list of possible co-authors
	 * @return
	 */
	@Cacheable("possibleCoAuthors")
	public Iterable<User> users() {
		return userRepository.findAllWhereLastonlineExistsOrderByUsername();
	}

	@Override
	public boolean css() {
		return true;
	}

	@Override
	public String cssHref() {
		return "/css/series.css";
	}

	@Override
	public Map<String, ?> getSettings() {
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("genres", genres());
		settings.put("ratings", ratings());
		settings.put("users", users());
		return settings;
	}
	
	@Override
	public boolean includeTimelineTemplateHtmlFragment() {
		return true;
	}

	@Override
	public String includeTimelineTemplateHtmlFragmentName() {
		return "series/fragments_series :: timeline_template_series";
	}
	
	@Override
	public boolean includeTimelineTemplateScriptFragment() {
		return true;
	}

	@Override
	public String includeTimelineTemplateScriptFragmentName() {
		return "series/fragments_series :: timeline_template_series_JS";
	}

	@Override
	public boolean includeHtmlFragment() {
		return true;
	}

	@Override
	public String includeHtmlFragmentName() {
		return "series/fragments_series :: series_settings";
	}

	@Override
	public boolean includeScriptFragment() {
		return true;
	}

	@Override
	public String includeScriptFragmentName() {
		return "series/fragments_series :: series_settings_JS";
	}

	@Override
	public boolean includeNewFormModal() {
		return true;
	}
	
	@Override
	public String includeNewFormModalContentFragmentName() {
		return "series/fragments_series :: newSeriesForm_model";
	}
	
	@Override
	public String newFormModalId() {
		return "newSeriesModal";
	}
	

}
