package net.mixednutz.app.server.manager.post.series.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.InternalTimelineElement.Type;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.manager.ApiElementConverter;

@Component
public class SeriesEntityConverter implements ApiElementConverter<Series> {

	@Autowired
	private NetworkInfo networkInfo;
	
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
		return Series.class.isInstance(entityClazz);
	}

}
