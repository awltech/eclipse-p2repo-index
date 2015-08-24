package com.worldline.mojo.p2repoindex;

import java.util.HashSet;
import java.util.Set;

public class GroupMapping {

	private String groupId;

	private Set<P2Identifier> installedFeatureIDs = new HashSet<P2Identifier>();

	public GroupMapping(final String groupId) {
		this.groupId = groupId;
	}

	public Set<P2Identifier> getInstalledFeatureIDs() {
		return installedFeatureIDs;
	}

	public String getGroupId() {
		return groupId;
	}

}
