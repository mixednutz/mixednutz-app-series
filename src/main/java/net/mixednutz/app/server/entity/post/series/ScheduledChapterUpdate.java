package net.mixednutz.app.server.entity.post.series;

import java.time.ZonedDateTime;

import javax.persistence.ConstraintMode;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.mixednutz.app.server.entity.post.AbstractScheduledPostUpdate;

@Entity
@DiscriminatorValue(ScheduledChapterUpdate.SCHEDULED_UPDATE_TYPE)
public class ScheduledChapterUpdate extends AbstractScheduledPostUpdate {
	
	public static final String SCHEDULED_UPDATE_TYPE = "Chapter";
	
	private Chapter chapter;
	private Chapter inReplyTo;
	
	public ScheduledChapterUpdate() {
		super(SCHEDULED_UPDATE_TYPE);
	}
	
	public ScheduledChapterUpdate(ZonedDateTime effectiveDate) {
		super(SCHEDULED_UPDATE_TYPE, effectiveDate);
	}

	public ScheduledChapterUpdate(ZonedDateTime effectiveDate, Chapter chapter, Chapter inReplyTo) {
		this(effectiveDate);
		this.chapter = chapter;
		this.inReplyTo = inReplyTo;
	}

	public static ScheduledChapterUpdate with(ZonedDateTime effectiveDate, Chapter chapter, Chapter inReplyTo) {
		return new ScheduledChapterUpdate(effectiveDate, chapter, inReplyTo);
	}

	@ManyToOne()
	@JoinColumn(name="chapter_id",
		foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT))
	public Chapter getChapter() {
		return chapter;
	}

	public void setChapter(Chapter chapter) {
		this.chapter = chapter;
	}

	@ManyToOne()
	@JoinColumn(name="in_reply_to",
		foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT))
	public Chapter getInReplyTo() {
		return inReplyTo;
	}

	public void setInReplyTo(Chapter inReplyTo) {
		this.inReplyTo = inReplyTo;
	}

	@Override
	public Chapter post() {
		return chapter;
	}

	@Override
	public Chapter inReplyTo() {
		return inReplyTo;
	}

}
