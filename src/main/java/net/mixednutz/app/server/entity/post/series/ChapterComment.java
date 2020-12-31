package net.mixednutz.app.server.entity.post.series;


import javax.persistence.ConstraintMode;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.mixednutz.app.server.entity.post.AbstractPostComment;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.entity.post.PostComment;

@Entity
@DiscriminatorValue(ChapterComment.CHAPTER_COMMENT_TYPE)
public class ChapterComment extends AbstractPostComment {
	
	public static final String CHAPTER_COMMENT_TYPE = "SeriesChapter";
	
	private Chapter chapter;

	public ChapterComment(String type) {
		super(CHAPTER_COMMENT_TYPE);
	}

	@JsonIgnore
	@ManyToOne()
	@JoinColumn(name="chapter_id",
		foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT))
	public Chapter getChapter() {
		return chapter;
	}

	public void setChapter(Chapter chapter) {
		this.chapter = chapter;
	}
	
	@Transient
	public String getUri() {
		return chapter.getUri()+"#"+getCommentId();
	}

	@Override
	public <P extends Post<C>, C extends PostComment> void setPost(P post) {
		this.chapter = (Chapter) post;
	}
	
	

}
