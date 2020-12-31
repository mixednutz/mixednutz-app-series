package net.mixednutz.app.server.entity.post.series;


import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.mixednutz.app.server.entity.post.AbstractPost;

@Entity
@Table(name="Series_Chapter")
public class Chapter extends AbstractPost<ChapterComment> {
	
	private Series series;
	
	private String title;
	private String titleKey;
	
	@OneToMany(mappedBy="chapter", fetch=FetchType.EAGER, cascade={CascadeType.REMOVE})
	@OrderBy("dateCreated asc")
	public List<ChapterComment> getComments() {
		return comments;
	}
	
	public void setComments(List<ChapterComment> comments) {
		this.comments = comments;
	}
	
	@JsonIgnore
	@ManyToOne()
	@JoinColumn(name="series_id",
		foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT))
	public Series getSeries() {
		return series;
	}

	public void setSeries(Series series) {
		this.series = series;
	}
	
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

	@Transient
	public String getUri() {
		if (getTitleKey()!=null && getOwner()!=null && getOwner().getUsername()!=null) {
			return series.getUri()+"/chapter/"+getTitleKey();
		}
		return series.getUri()+"/chapter/id/"+getId();
	}

}
