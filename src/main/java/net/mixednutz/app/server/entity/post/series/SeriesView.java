package net.mixednutz.app.server.entity.post.series;


import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.mixednutz.app.server.entity.post.AbstractPostView;

@Entity
public class SeriesView extends AbstractPostView{
	
	private Series series;

	@ManyToOne
	@JoinColumn(name=ViewPK.POST_ID_COLUMN_NAME, insertable=false, updatable=false)
	public Series getSeries() {
		return series;
	}

	public void setSeries(Series series) {
		this.series = series;
	}
	
}
