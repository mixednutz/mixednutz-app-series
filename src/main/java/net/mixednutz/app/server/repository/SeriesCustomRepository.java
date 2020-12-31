package net.mixednutz.app.server.repository;

import java.util.List;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Series;

public interface SeriesCustomRepository {

	public List<Series> getNutsterzSeriesForTag(User user, String tag, Long excludeId);
	
}
