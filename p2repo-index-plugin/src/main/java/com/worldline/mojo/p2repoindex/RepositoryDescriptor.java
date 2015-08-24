package com.worldline.mojo.p2repoindex;

import java.util.ArrayList;
import java.util.List;

/**
 * Data object describing a repository
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public class RepositoryDescriptor {

	/**
	 * Repository name
	 */
	private String name;

	/**
	 * Repository timestamp
	 */
	private String timestamp;

	/**
	 * Features of repository
	 */
	private List<CategoryDescriptor> categoryDescriptors = new ArrayList<CategoryDescriptor>();

	/**
	 * @param Repository
	 *            name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Repository name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return categories
	 */
	public List<CategoryDescriptor> getCategoryDescriptors() {
		return categoryDescriptors;
	}

	/**
	 * 
	 * @return repository timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * @param repository
	 *            timestamp
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
