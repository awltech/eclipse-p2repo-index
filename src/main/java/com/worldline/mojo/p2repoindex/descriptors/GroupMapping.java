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

import java.util.HashSet;
import java.util.Set;

/**
 * Technical class that is used to make the group mapping, to be able to
 * associate categories and features.
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public class GroupMapping {

	/**
	 * Identifier of the group
	 */
	private String groupId;

	/**
	 * Identifier of the features to be installed within the group
	 */
	private Set<P2Identifier> installedFeatureIDs = new HashSet<P2Identifier>();

	/**
	 * Creates new group with identified
	 * 
	 * @param groupId
	 */
	public GroupMapping(final String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return installable features within this group
	 */
	public Set<P2Identifier> getInstalledFeatureIDs() {
		return installedFeatureIDs;
	}

	/**
	 * @return ID of the group
	 */
	public String getGroupId() {
		return groupId;
	}

}
