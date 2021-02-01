package net.mixednutz.app.server.entity.post.series;

import java.util.Collections;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.AbstractCommentNotification;
import net.mixednutz.app.server.entity.post.AbstractReactionNotification;
import net.mixednutz.app.server.entity.post.PostNotification;
import net.mixednutz.app.server.entity.post.PostReaction;
import net.mixednutz.app.server.manager.NotificationManager.PostNotificationFactory;
import net.mixednutz.app.server.repository.PostNotificationRepository;

@Component
public class SeriesNotificationFactory implements PostNotificationFactory<Series, SeriesReview, PostReaction> {
		
	@Autowired
	PostNotificationRepository notificationRepository;
	
	@Override
	public boolean canConvert(Class<?> postEntityClazz) {
		return Series.class.isAssignableFrom(postEntityClazz);
	}

	@Override
	public PostNotification createCommentNotification(Series replyTo, SeriesReview comment) {
		return new SeriesCommentNotification(replyTo.getAuthorId(), replyTo, comment);
	}

	@Override
	public PostNotification createReactionNotification(Series reactedTo, PostReaction reaction) {
		return null;
	}

	@Override
	public Iterable<? extends AbstractCommentNotification<Series, SeriesReview>> lookupCommentNotifications(User user,
			Series post) {
		return notificationRepository.loadNotifications((criteriaBuilder, itemRoot) ->{
			return criteriaBuilder.and(
					criteriaBuilder.equal(itemRoot.get("seriesId"), post.getId()),
					criteriaBuilder.equal(itemRoot.get("userId"), user.getUserId()));
		}, SeriesCommentNotification.class);
	}

	@Override
	public Iterable<? extends AbstractReactionNotification<Series, SeriesReview, PostReaction>> lookupReactionNotifications(
			User user, Series reactedTo) {
		return Collections.emptyList();
	}

	@Entity
	@DiscriminatorValue(SeriesCommentNotification.TYPE)
	public static class SeriesCommentNotification extends AbstractCommentNotification<Series, SeriesReview> {
		
		public static final String TYPE = "NewSeriesComment";
		private static final String TYPE_DISPLAY = "series";
		
		public SeriesCommentNotification(Long userId, Series series, SeriesReview comment) {
			super(TYPE, userId, series, comment);
		}

		public SeriesCommentNotification() {
			super(TYPE);
		}

		@ManyToOne
		@JoinColumn(name="comment_id", insertable=false, updatable=false)
		public SeriesReview getComment() {
			return comment;
		}
		@ManyToOne
		@JoinColumn(name="series_id", insertable=false, updatable=false)
		public Series getSeries() {
			return post;
		}
		@Column(name="series_id", insertable=true, updatable=false)
		public Long getSeriesId() {
			return postId;
		}
		

		public void setSeries(Series series) {
			this.post = series;
		}
		public void setSeriesId(Long seriesId) {
			this.postId = seriesId;
		}

		@Transient
		public String getPostTypeDisplayName() {
			return TYPE_DISPLAY;
		}
		@Transient
		public String getPostSubject() {
			return post.getTitle();
		}
		
	}
	
}
