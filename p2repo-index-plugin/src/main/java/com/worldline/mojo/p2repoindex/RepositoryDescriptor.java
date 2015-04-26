package com.worldline.mojo.p2repoindex;

import java.util.ArrayList;
import java.util.List;

public class RepositoryDescriptor {

	private String name;
	
	private String timestamp;

	public RepositoryDescriptor() {
	}
	
	public void setName(String name) {
		this.name = name;
	}

	private List<FeatureDescriptor> featureDescriptors = new ArrayList<FeatureDescriptor>();

	public String getName() {
		return name;
	}

	public List<FeatureDescriptor> getFeatureDescriptors() {
		return featureDescriptors;
	}

	public String getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
