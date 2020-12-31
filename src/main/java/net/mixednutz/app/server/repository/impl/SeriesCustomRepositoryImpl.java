package net.mixednutz.app.server.repository.impl;

import java.util.Collections;
import java.util.List;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.repository.SeriesCustomRepository;

public class SeriesCustomRepositoryImpl implements SeriesCustomRepository {

	@Override
	public List<Series> getNutsterzSeriesForTag(User user, String tag, Long excludeId) {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

}
