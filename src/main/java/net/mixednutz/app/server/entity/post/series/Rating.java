package net.mixednutz.app.server.entity.post.series;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Rating {
	
	private String id;
	private Integer sortOrder;
	private String displayName;
	private String description;
	
	private boolean requiresAgeVerification;
	private Integer minimumAge;
	
	@Id
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
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
	public boolean isRequiresAgeVerification() {
		return requiresAgeVerification;
	}
	public void setRequiresAgeVerification(boolean requiresAgeVerification) {
		this.requiresAgeVerification = requiresAgeVerification;
	}
	public Integer getMinimumAge() {
		return minimumAge;
	}
	public void setMinimumAge(Integer minimumAge) {
		this.minimumAge = minimumAge;
	}
	
}
