package net.mixednutz.app.server.entity.post.series;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.AbstractTag;

@Entity
@DiscriminatorValue(SeriesTag.TYPE)
@JsonTypeName(SeriesTag.TYPE)
@JsonInclude(JsonInclude.Include.NON_NULL)
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

	@JsonIgnore
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
