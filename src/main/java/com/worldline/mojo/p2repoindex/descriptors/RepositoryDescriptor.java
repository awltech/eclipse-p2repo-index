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
	private long timestamp;

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
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param repository
	 *            timestamp
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
