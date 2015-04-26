package com.worldline.mojo.p2repoindex;

public class FeatureDescriptor implements Comparable<FeatureDescriptor> {

	private String id;

	private String name;

	private String version;

	public FeatureDescriptor(String id, String name, String version) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public int compareTo(FeatureDescriptor o) {
		return id.compareTo(o.id);
	}

	@Override
	public String toString() {
		return String.format("[ID: %s, VERSION: %s, NAME: %s]", id, version, name);
	}

}
