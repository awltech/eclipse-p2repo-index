package com.worldline.mojo.p2repoindex;

import java.util.ArrayList;
import java.util.List;

public class CategoryDescriptor implements Comparable<CategoryDescriptor> {

	private String name;

	private List<FeatureDescriptor> featureDescriptors = new ArrayList<FeatureDescriptor>();

	public CategoryDescriptor(String name) {
		this.name = name;
	}

	public List<FeatureDescriptor> getFeatureDescriptors() {
		return featureDescriptors;
	}

	public String getName() {
		return name;
	}

	public int compareTo(CategoryDescriptor o) {
		return name != null ? name.compareTo(o.name) : -1;
	}

}
