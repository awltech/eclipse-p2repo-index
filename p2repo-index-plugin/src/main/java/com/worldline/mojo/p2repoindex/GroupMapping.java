package com.worldline.mojo.p2repoindex;

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
