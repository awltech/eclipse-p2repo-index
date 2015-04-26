package com.worldline.mojo.p2repoindex;

import java.util.ArrayList;
import java.util.List;

public class RepositoryDescriptor {

	private String name;

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

}
