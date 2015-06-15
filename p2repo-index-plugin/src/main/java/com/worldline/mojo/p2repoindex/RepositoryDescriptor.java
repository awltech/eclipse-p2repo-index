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
	private List<FeatureDescriptor> featureDescriptors = new ArrayList<FeatureDescriptor>();

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
	 * @return features of repository
	 */
	public List<FeatureDescriptor> getFeatureDescriptors() {
		return featureDescriptors;
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
