package net.mixednutz.app.server.entity.post.series;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
import net.mixednutz.app.server.entity.ExternalFeedContent;
import net.mixednutz.app.server.entity.ReactionsAware;

@Entity
@Table(name="Series_Chapter")
public class Chapter extends AbstractChapter<ChapterComment> implements
	CommentsAware<ChapterComment>, ReactionsAware<ChapterReaction>{
	
	private boolean hasExplictSexualContent;
	
	// Foreign table mappings
	private Series series;
	private ScheduledChapter scheduled;
	private List<ChapterComment> comments;
	private Set<ChapterReaction> reactions;
	private Set<ExternalFeedContent> crossposts;
	private Set<ChapterView> views;
	
	// Transient fields
	private String filteredBody;
	private Long wordCount;
	private Long readingTime;
	
	
	public Boolean getHasExplictSexualContent() {
		return hasExplictSexualContent;
	}

	public void setHasExplictSexualContent(Boolean explictSexualContent) {
		this.hasExplictSexualContent = explictSexualContent!=null?explictSexualContent:false;
	}
		
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

	@JoinTable(name="Series_Chapter_Crossposts")
	@ManyToMany(cascade=CascadeType.ALL)
	public Set<ExternalFeedContent> getCrossposts() {
		return crossposts;
	}

	public void setCrossposts(Set<ExternalFeedContent> crossposts) {
		this.crossposts = crossposts;
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
	public Long getReadingTime() {
		return readingTime;
	}

	public void setReadingTime(Long readingTime) {
		this.readingTime = readingTime;
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
