package net.mixednutz.app.server.controller.exception;

import net.mixednutz.app.server.entity.post.AbstractPost;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.Series;

public class ForbiddenExceptions {

	abstract static class BaseForbiddenException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		protected AbstractPost<?> post;

		public BaseForbiddenException(AbstractPost<?> post, String message) {
			super(message);
			this.post = post;
		}
		
	}
	
	public static class ChapterForbiddenException extends BaseForbiddenException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4505331672264632131L;

		public ChapterForbiddenException(Chapter chapter, String message) {
			super(chapter, message);
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

		public SeriesForbiddenException(Series series, String message) {
			super(series, message);
		}
		
		public Series getSeries() {
			return (Series) post;
		}
		
	}
	
}
