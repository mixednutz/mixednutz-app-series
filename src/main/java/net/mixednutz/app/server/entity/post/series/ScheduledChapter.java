package net.mixednutz.app.server.entity.post.series;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import net.mixednutz.app.server.entity.post.AbstractScheduledPost;
import net.mixednutz.app.server.entity.post.Post;

@Entity
@DiscriminatorValue(ScheduledChapter.SCHEDULED_POST_TYPE)
public class ScheduledChapter extends AbstractScheduledPost {
	
	public static final String SCHEDULED_POST_TYPE = "SeriesChapter";

	private Chapter chapter;
	
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

	@Override
	public Post<?> post() {
		return chapter;
	}

}
