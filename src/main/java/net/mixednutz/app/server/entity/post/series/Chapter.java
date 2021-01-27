package net.mixednutz.app.server.entity.post.series;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.mixednutz.app.server.entity.CommentsAware;
import net.mixednutz.app.server.entity.ReactionsAware;

@Entity
@Table(name="Series_Chapter")
public class Chapter extends AbstractChapter<ChapterComment> implements
	CommentsAware<ChapterComment>, ReactionsAware<ChapterReaction>{
	
	private Series series;
	
	private ScheduledChapter scheduled;

	private List<ChapterComment> comments;
	private Set<ChapterReaction> reactions;
	private Set<ChapterView> views;
	
	private String filteredBody;
	private Long wordCount;
		
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="scheduled_id")
	public ScheduledChapter getScheduled() {
		return scheduled;
	}

	public void setScheduled(ScheduledChapter scheduled) {
		this.scheduled = scheduled;
	}

	@OneToMany(mappedBy="chapter", cascade={CascadeType.REMOVE})
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
	
	@Fetch(FetchMode.SELECT)
	@OneToMany(mappedBy="chapter", cascade=CascadeType.ALL, orphanRemoval=true)
	public Set<ChapterReaction> getReactions() {
		return reactions;
	}

	public void setReactions(Set<ChapterReaction> reactions) {
		this.reactions = reactions;
	}

	@OneToMany(mappedBy="chapter", orphanRemoval=true)
	public Set<ChapterView> getViews() {
		return views;
	}

	public void setViews(Set<ChapterView> views) {
		this.views = views;
	}

	@Transient
	public String getFilteredBody() {
		return filteredBody;
	}

	public void setFilteredBody(String filteredBody) {
		this.filteredBody = filteredBody;
	}

	@Transient
	public Long getWordCount() {
		return wordCount;
	}

	public void setWordCount(Long wordCount) {
		this.wordCount = wordCount;
	}

	@Transient
	public String getUri() {
		if (getTitleKey()!=null && getOwner()!=null && getOwner().getUsername()!=null) {
			return series.getUri()+
					"/chapter/"+getId()+"/"+
					getTitleKey();
		}
		return series.getUri()+"/chapter/id/"+getId();
	}

}
