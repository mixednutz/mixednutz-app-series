package net.mixednutz.app.server.manager.post.series.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.mixednutz.app.server.entity.ComponentSettings;
import net.mixednutz.app.server.entity.post.series.Genre;
import net.mixednutz.app.server.entity.post.series.Rating;
import net.mixednutz.app.server.repository.GenreRepository;
import net.mixednutz.app.server.repository.RatingRepository;

@Component
public class SeriesSettingsManager implements ComponentSettings {
	
	@Autowired
	private GenreRepository genreRepository;
	
	@Autowired
	private RatingRepository ratingRepository;
	
	//TODO tag groups (ie character age
	//TODO permanant tags
	

	public Iterable<Genre> genres() {
		return genreRepository.findByOrderByDisplayNameAsc();
	}
	
	public Iterable<Rating> ratings() {
		return ratingRepository.findByOrderBySortOrderAsc();
	}

	@Override
	public Map<String, ?> getSettings() {
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("genres", genres());
		settings.put("ratings", ratings());
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
