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

/**
 * Data pojo describing a feature, read from content.xml file.
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public class FeatureDescriptor implements Comparable<FeatureDescriptor> {

	/**
	 * Feature id
	 */
	private String id;

	/**
	 * Feature name
	 */
	private String name;

	/**
	 * Feature version
	 */
	private String version;

	/**
	 * Feature provider
	 */
	private String provider;

	/**
	 * Creates new descriptor
	 * 
	 * @param id
	 * @param name
	 * @param version
	 * @param provider
	 */
	public FeatureDescriptor(String id, String name, String version, String provider) {
		super();
		this.id = id != null ? id : "";
		this.name = name != null ? name : "";
		this.version = version != null ? version : "";
		this.provider = provider != null ? provider : "";
	}

	/**
	 * @return feature id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return feature name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return feature version
	 */
	public String getVersion() {
		return version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(FeatureDescriptor o) {
		return id.compareTo(o.id) != 0 ? id.compareTo(o.id) : -version.compareTo(o.version);
	}

	/**
	 * @return feature provider
	 */
	public String getProvider() {
		return this.provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s (%s)", id, version);
	}

}
