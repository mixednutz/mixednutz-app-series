package net.mixednutz.app.server.controller.exception;

import net.mixednutz.app.server.entity.post.AbstractPost;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.manager.post.PostManager.NotVisibleType;

public class ForbiddenExceptions {

	abstract static class BaseForbiddenException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		protected final AbstractPost<?> post;
		private final NotVisibleType type;

		public BaseForbiddenException(AbstractPost<?> post, NotVisibleType type, String message) {
			super(message);
			this.post = post;
			this.type = type;
		}

		public NotVisibleType getType() {
			return type;
		}
		
	}
	
	public static class ChapterForbiddenException extends BaseForbiddenException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4505331672264632131L;

		public ChapterForbiddenException(Chapter chapter, NotVisibleType type, String message) {
			super(chapter, type, message);
		}
		
		public Chapter getChapter() {
			return (Chapter) post;
		}
		
	}
	
	public static class SeriesForbiddenException extends BaseForbiddenException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4505331672264632131L;

		public SeriesForbiddenException(Series series, NotVisibleType type, String message) {
			super(series, type, message);
		}
		
		public Series getSeries() {
			return (Series) post;
		}
		
	}
	
}
