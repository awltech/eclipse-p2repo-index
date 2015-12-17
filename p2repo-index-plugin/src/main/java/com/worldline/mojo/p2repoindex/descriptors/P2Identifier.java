package com.worldline.mojo.p2repoindex.descriptors;

/**
 * Technical class used to identify a p2 element. It is with its name and
 * version
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public class P2Identifier {

	/**
	 * Identifier name part
	 */
	private String name;

	/**
	 * Identifier version part
	 */
	private String version;

	/**
	 * @return name part of the identifier
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return version part of the identifier
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Creates new identifier
	 * 
	 * @param name
	 * @param version
	 */
	public P2Identifier(String name, String version) {
		super();
		this.name = name;
		this.version = version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		P2Identifier other = (P2Identifier) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}

}
