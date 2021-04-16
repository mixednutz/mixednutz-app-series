package net.mixednutz.app.server.entity.post.series;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.springframework.stereotype.Component;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.AbstractCommentNotification;
import net.mixednutz.app.server.entity.post.AbstractCommentReplyNotification;
import net.mixednutz.app.server.entity.post.AbstractPostComment;
import net.mixednutz.app.server.entity.post.AbstractReactionNotification;
import net.mixednutz.app.server.entity.post.PostNotification;
import net.mixednutz.app.server.manager.NotificationManager.PostNotificationFactory;
import net.mixednutz.app.server.manager.impl.BaseNotificationFactory;

@Component
public class ChapterNotificationFactory extends BaseNotificationFactory implements PostNotificationFactory<Chapter, ChapterComment, ChapterReaction> {
	
	@Override
	public boolean canConvert(Class<?> postEntityClazz) {
		return Chapter.class.isAssignableFrom(postEntityClazz);
	}

	@Override
	public PostNotification createCommentNotification(Chapter replyTo, ChapterComment comment) {
		return new ChapterCommentNotification(replyTo.getAuthorId(), replyTo, comment);
	}

	@Override
	public PostNotification createReactionNotification(Chapter reactedTo, ChapterReaction reaction) {
		return new ChapterReactionNotification(reactedTo.getAuthorId(), reactedTo, reaction);
	}

	@Override
	public Iterable<? extends AbstractCommentNotification<Chapter, ChapterComment>> lookupCommentNotifications(
			User user, Chapter post) {
		return notificationRepository.loadNotifications((criteriaBuilder, itemRoot) ->{
			return criteriaBuilder.and(
					criteriaBuilder.equal(itemRoot.get("chapterId"), post.getId()),
					criteriaBuilder.equal(itemRoot.get("userId"), user.getUserId()));
		}, ChapterCommentNotification.class);
	}
	
	@Override
	public Iterable<? extends AbstractCommentReplyNotification<? extends AbstractPostComment>> lookupCommentReplyNotifications(
			User user, Chapter post) {
		return lookupCommentReplyNotifications(user, post.getComments());
	}

	@Override
	public Iterable<? extends AbstractReactionNotification<Chapter, ChapterComment, ChapterReaction>> lookupReactionNotifications(
			User user, Chapter reactedTo) {
		return notificationRepository.loadNotifications((criteriaBuilder, itemRoot) ->{
			return criteriaBuilder.and(
					criteriaBuilder.equal(itemRoot.get("chapterId"), reactedTo.getId()),
					criteriaBuilder.equal(itemRoot.get("userId"), user.getUserId()));
		}, ChapterReactionNotification.class);
	}

	@Entity
	@DiscriminatorValue(ChapterCommentNotification.TYPE)
	public static class ChapterCommentNotification extends AbstractCommentNotification<Chapter, ChapterComment> {
		
		public static final String TYPE = "NewChapterComment";
		private static final String TYPE_DISPLAY = "chapter";
		
		public ChapterCommentNotification(Long userId, Chapter chapter, ChapterComment comment) {
			super(TYPE, userId, chapter, comment);
		}

		public ChapterCommentNotification() {
			super(TYPE);
		}

		@ManyToOne
		@JoinColumn(name="comment_id", insertable=false, updatable=false)
		public ChapterComment getComment() {
			return comment;
		}
		@ManyToOne
		@JoinColumn(name="chapter_id", insertable=false, updatable=false)
		public Chapter getChapter() {
			return post;
		}
		@Column(name="chapter_id", insertable=true, updatable=false)
		public Long getChapterId() {
			return postId;
		}
		

		public void setChapter(Chapter chapter) {
			this.post = chapter;
		}
		public void setChapterId(Long chapterId) {
			this.postId = chapterId;
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
	
	@Entity
	@DiscriminatorValue(ChapterReactionNotification.TYPE)
	public static class ChapterReactionNotification extends AbstractReactionNotification<Chapter, ChapterComment, ChapterReaction> {
		
		public static final String TYPE = "ChapterReaction";
		private static final String TYPE_DISPLAY = "chapter";
		
		public ChapterReactionNotification(Long userId, Chapter chapter, ChapterReaction reaction) {
			super(TYPE, userId, chapter, reaction);
		}

		public ChapterReactionNotification() {
			super(TYPE);
		}
		
		@ManyToOne()
		@JoinColumn(name="reaction_id", insertable=false, updatable=false)
		public ChapterReaction getReaction() {
			return reaction;
		}
		@Column(name="chapter_id", insertable=true, updatable=false)
		public Long getChapterId() {
			return reactedToId;
		}
		@ManyToOne()
		@JoinColumn(name="chapter_id", insertable=false, updatable=false)
		public Chapter getChapter() {
			return reactedTo;
		}
		
		public void setChapter(Chapter chapter) {
			this.reactedTo = chapter;
		}
		public void setChapterId(Long chapterId) {
			this.reactedToId = chapterId;
		}

		@Transient
		public String getReactedToTypeDisplayName() {
			return TYPE_DISPLAY;
		}
		@Transient
		public String getReactedToSubject() {
			return reactedTo.getTitle();
		}
		
	}
	
}
