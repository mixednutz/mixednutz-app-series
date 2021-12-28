package net.mixednutz.app.server.entity.post.series;


import javax.persistence.ConstraintMode;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.mixednutz.app.server.entity.post.AbstractPostComment;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.entity.post.PostComment;

@Entity
@DiscriminatorValue(SeriesReview.SERIES_REVIEW_TYPE)
public class SeriesReview extends AbstractPostComment {

	public static final String SERIES_REVIEW_TYPE = "Series";
	
	private Series series;
	
	public SeriesReview() {
		super(SERIES_REVIEW_TYPE);
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
	
	@Transient
	public String getUri() {
		return series.getUri()+"#"+getCommentId();
	}

	@Override
	public <P extends Post<C>, C extends PostComment> void setPost(P post) {
		this.series = (Series) post;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transient
	public <P extends Post<C>, C extends PostComment> P getPost() {
		return (P) series;
	}
	
}
