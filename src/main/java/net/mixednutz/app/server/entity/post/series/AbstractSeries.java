package net.mixednutz.app.server.entity.post.series;


import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import net.mixednutz.app.server.entity.post.AbstractPost;
import net.mixednutz.app.server.entity.post.AbstractPostComment;

@MappedSuperclass
public abstract class AbstractSeries<C extends AbstractPostComment> 
	extends AbstractPost<C>  {

	private String title;
	private String titleKey;
	
	@Column(name="title")
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	@Column(name="title_key")
	public String getTitleKey() {
		return titleKey;
	}
	public void setTitleKey(String titleKey) {
		this.titleKey = titleKey;
	}

}
