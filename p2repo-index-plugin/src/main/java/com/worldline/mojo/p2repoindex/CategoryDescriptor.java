package com.worldline.mojo.p2repoindex;

import java.util.ArrayList;
import java.util.List;

/**
 * Pojo that holds information about the category descriptor & included features
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public class CategoryDescriptor implements Comparable<CategoryDescriptor> {

	/**
	 * Category name
	 */
	private String name;

	/**
	 * List of included features
	 */
	private List<FeatureDescriptor> featureDescriptors = new ArrayList<FeatureDescriptor>();

	/**
	 * Creates new category descriptor
	 * 
	 * @param name
	 */
	public CategoryDescriptor(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return list of included features
	 */
	public List<FeatureDescriptor> getFeatureDescriptors() {
		return featureDescriptors;
	}

	/**
	 * 
	 * @return category name
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(CategoryDescriptor o) {
		return name != null ? name.compareTo(o.name) : -1;
	}

}
