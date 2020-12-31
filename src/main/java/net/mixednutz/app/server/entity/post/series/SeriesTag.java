package net.mixednutz.app.server.entity.post.series;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.AbstractTag;

@Entity
@DiscriminatorValue(SeriesTag.TYPE)
public class SeriesTag extends AbstractTag {

	public static final String TYPE = "Series";
	
	private Series series;
	
	public SeriesTag(Series series, String tag) {
		super(TYPE, tag);
		this.series = series;
	}
	
	public SeriesTag(Series series, String tag, User user) {
		super(TYPE, tag, user.getUserId());
		this.series = series;
	}

	public SeriesTag() {
		super(TYPE);
	}

	@ManyToOne()
	@JoinColumn(name="series_id")
	@NotFound(action=NotFoundAction.IGNORE)
	public Series getSeries() {
		return series;
	}

	public void setSeries(Series series) {
		this.series = series;
	}
	
}
