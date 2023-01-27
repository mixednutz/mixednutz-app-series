package net.mixednutz.app.server.manager.post.series.impl;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.Oembeds.Oembed;
import net.mixednutz.app.server.entity.post.series.SeriesReview;
import net.mixednutz.app.server.manager.post.impl.AbstractPostCommentEntityConverter;

@Component
public class SeriesReviewEntityConverter extends AbstractPostCommentEntityConverter<SeriesReview> {

	@Override
	public boolean canConvert(Class<?> entityClazz) {
		return SeriesReview.class.isAssignableFrom(entityClazz);
	}

	@Override
	public Oembed toOembed(String path, Integer maxwidth, Integer maxheight, String format, Authentication auth,
			String baseUrl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canConvertOembed(String path) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void populatePostProperties(InternalTimelineElement api, SeriesReview entity) {
		api.setInReplyToTitle(entity.getSeries().getTitle());
	}

}
