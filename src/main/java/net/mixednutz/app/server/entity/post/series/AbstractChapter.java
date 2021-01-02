package net.mixednutz.app.server.entity.post.series;


import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

import net.mixednutz.app.server.entity.post.AbstractPost;
import net.mixednutz.app.server.entity.post.AbstractPostComment;

@MappedSuperclass
public abstract class AbstractChapter<C extends AbstractPostComment> 
	extends AbstractPost<C>  {

	private String title;
	private String titleKey;
	private String body; 
	
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
	@Lob
	@Column(name="body")
//	@Column(name="body", columnDefinition="LONGTEXT")
	public String getBody() {
		return body;
	}		
	public void setBody(String body) {
		this.body = body;
	}

}
