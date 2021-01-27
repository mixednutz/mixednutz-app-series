package net.mixednutz.app.server.entity.post.series;


import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.mixednutz.app.server.entity.CommentsAware;
import net.mixednutz.app.server.entity.post.GroupedPosts;

@Entity
@Table(name="Series")
public class Series extends AbstractSeries<SeriesReview> 
	implements GroupedPosts<Chapter, ChapterComment>, CommentsAware<SeriesReview> {
	
	private List<SeriesReview> reviews;
	private Set<SeriesTag> tags;
	private Set<SeriesView> views;

	private List<Chapter> chapters;
	
	private Genre genre;
	private Set<Genre> additionalGenres;
	private Rating rating;
	private Status status = Status.IN_PROGRESS;
	
	@Override
	public void onPersist() {
		super.onPersist();
		setDatePublished(getDateCreated());
	}
	
	@PreUpdate
	public void onUpdate() {
		if (getDatePublished()==null) {
			setDatePublished(getDateCreated());
		}
	}

	@OneToMany(mappedBy="series", cascade={CascadeType.REMOVE})
	@OrderBy("dateCreated asc")
	public List<SeriesReview> getReviews() {
		return reviews;
	}
	
	@JsonIgnore
	@Transient
	public List<SeriesReview> getComments() {
		return getReviews();
	}
	
	@Fetch(FetchMode.SELECT)
	@OneToMany(mappedBy="series", cascade=CascadeType.ALL, orphanRemoval=true)
	public Set<SeriesTag> getTags() {
		return tags;
	}
	
	@OneToMany(mappedBy="series", orphanRemoval=true)
	public Set<SeriesView> getViews() {
		return views;
	}
	
	@OneToMany(mappedBy="series", cascade={CascadeType.REMOVE})
	@OrderBy("dateCreated asc")
	public List<Chapter> getChapters() {
		return chapters;
	}
	
	@ManyToOne
	public Genre getGenre() {
		return genre;
	}

	@ManyToMany(targetEntity=Genre.class, fetch=FetchType.EAGER, 
			cascade={CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name="Series_Genre")
	public Set<Genre> getAdditionalGenres() {
		return additionalGenres;
	}

	@ManyToOne
	public Rating getRating() {
		return rating;
	}

	@Enumerated
	public Status getStatus() {
		return status;
	}

	@Transient
	public String getUri() {
		if (getTitleKey()!=null && getOwner()!=null && getOwner().getUsername()!=null) {
			return "/"+getOwner().getUsername().replaceAll(" ", "")+
					"/series/"+getId()+"/"+
					getTitleKey();
		}
		return "/series/id/"+getId();
	}

	@Override
	public void setPosts(List<Chapter> posts) {
		this.chapters = posts;
	}

	public void setChapters(List<Chapter> chapters) {
		this.chapters = chapters;
	}

	public void setReviews(List<SeriesReview> reviews) {
		this.reviews = reviews;
	}

	public void setTags(Set<SeriesTag> tags) {
		this.tags = tags;
	}

	public void setViews(Set<SeriesView> views) {
		this.views = views;
	}

	public void setGenre(Genre genre) {
		this.genre = genre;
	}

	public void setAdditionalGenres(Set<Genre> additionalGenres) {
		this.additionalGenres = additionalGenres;
	}
	
	public void setRating(Rating rating) {
		this.rating = rating;
	}

	public void setStatus(Status stautus) {
		this.status = stautus;
	}

}
