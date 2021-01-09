package net.mixednutz.app.server.entity.post.series;


import java.time.ZonedDateTime;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Genre {

	private String id;
	private String displayName;
	private String description;
	
	private ZonedDateTime dateCreated;
	private ZonedDateTime dateModified;
	
	private Set<Series> additionalSeries;

	@PrePersist
	void createdAt() {
		setDateCreated(ZonedDateTime.now());
		setDateModified(getDateCreated());
	}

	@PreUpdate
	void updatedAt() {
		setDateModified(ZonedDateTime.now());
	}
	
	@Id
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@JsonIgnore
	public ZonedDateTime getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(ZonedDateTime dateCreated) {
		this.dateCreated = dateCreated;
	}
	@JsonIgnore
	public ZonedDateTime getDateModified() {
		return dateModified;
	}
	public void setDateModified(ZonedDateTime dateModified) {
		this.dateModified = dateModified;
	}

	@ManyToMany(mappedBy="additionalGenres")
	public Set<Series> getAdditionalSeries() {
		return additionalSeries;
	}

	public void setAdditionalSeries(Set<Series> additionalSeries) {
		this.additionalSeries = additionalSeries;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Genre) {
			return id.equals(((Genre)obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
		
}
