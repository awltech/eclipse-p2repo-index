package com.worldline.mojo.p2repoindex;

public class FeatureDescriptor implements Comparable<FeatureDescriptor> {

	private String id;

	private String name;

	private String version;

	private String provider;

	public FeatureDescriptor(String id, String name, String version, String provider) {
		super();
		this.id = id != null ? id : "";
		this.name = name != null ? name : "";
		this.version = version != null ? version : "";
		this.provider = provider != null ? provider : "";
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
		return id.compareTo(o.id) != 0 ? id.compareTo(o.id) : version.compareTo(o.version);
	}

	public String getProvider() {
		return this.provider;
	}

	@Override
	public String toString() {
		return String.format("[ID: %s, VERSION: %s, NAME: %s, PROVIDER: %s]", id, version, name, provider);
	}

}
