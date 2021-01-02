package net.mixednutz.app.server.entity.post.series;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.mixednutz.app.server.entity.post.AbstractPostView;

@Entity
public class ChapterView extends AbstractPostView {
	
	private Chapter chapter;

	@ManyToOne
	@JoinColumn(name=ViewPK.POST_ID_COLUMN_NAME, insertable=false, updatable=false)
	public Chapter getChapter() {
		return chapter;
	}

	public void setChapter(Chapter chapter) {
		this.chapter = chapter;
	}
	
}
