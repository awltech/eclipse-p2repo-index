/**
 * eclipse-p2repo-index by Worldline
 *
 * Copyright (C) 2016 Worldline or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.worldline.mojo.p2repoindex.descriptors;

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
