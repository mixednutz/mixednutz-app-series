package net.mixednutz.app.server.entity.post.series;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.AbstractReaction;

@Entity
@DiscriminatorValue(ChapterReaction.TYPE)
@JsonTypeName(ChapterReaction.TYPE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChapterReaction extends AbstractReaction {

	public static final String TYPE = "Series_Chapter";
	
	private Chapter chapter;
	
	public ChapterReaction(Chapter chapter, String emojiId) {
		super(TYPE, emojiId);
		this.chapter = chapter;
	}
	
	public ChapterReaction(Chapter chapter, String emojiId, User user) {
		super(TYPE, emojiId, user.getUserId());
		this.chapter = chapter;
	}

	public ChapterReaction() {
		super(TYPE);
	}

	@JsonIgnore
	@ManyToOne()
	@JoinColumn(name="chapter_id")
	@NotFound(action=NotFoundAction.IGNORE)
	public Chapter getChapter() {
		return chapter;
	}

	public void setChapter(Chapter chapter) {
		this.chapter = chapter;
	}

	@Transient
	public String getParentSubject() {
		return chapter.getTitle();
	}

	@Transient
	public Long getParentAuthorId() {
		return chapter.getAuthorId();
	}

	@Transient
	public String getParentUri() {
		return chapter.getUri();
	}
	
}
