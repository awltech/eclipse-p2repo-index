package com.worldline.mojo.p2repoindex;

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
		return String.format("[ID: %s, VERSION: %s, NAME: %s, PROVIDER: %s]", id, version, name, provider);
	}

}
