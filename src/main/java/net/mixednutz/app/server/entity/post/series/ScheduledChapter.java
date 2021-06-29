package net.mixednutz.app.server.entity.post.series;

import javax.persistence.ConstraintMode;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import net.mixednutz.app.server.entity.post.AbstractScheduledPost;
import net.mixednutz.app.server.entity.post.Post;

@Entity
@DiscriminatorValue(ScheduledChapter.SCHEDULED_POST_TYPE)
public class ScheduledChapter extends AbstractScheduledPost {
	
	public static final String SCHEDULED_POST_TYPE = "SeriesChapter";

	private Chapter chapter;
	private Chapter inReplyTo;
	
	public ScheduledChapter() {
		super(SCHEDULED_POST_TYPE);
	}

	@OneToOne(mappedBy="scheduled", targetEntity=Chapter.class)
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
	public Post<?> post() {
		return chapter;
	}

	@Override
	public Post<?> inReplyTo() {
		return inReplyTo;
	}

}
