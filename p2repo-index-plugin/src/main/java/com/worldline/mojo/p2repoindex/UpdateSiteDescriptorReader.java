package com.worldline.mojo.p2repoindex;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Utility class that takes a content.xml stream as input, and returns a
 * descriptors object graph, for this repository.
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public class UpdateSiteDescriptorReader {

	/**
	 * Input of the processus
	 */
	private final InputStream updateSiteDescriptorStream;

	/**
	 * Output of the processus.
	 */
	private final RepositoryDescriptor repositoryDescriptor = new RepositoryDescriptor();

	/**
	 * Logger instance
	 */
	private Log log;

	// Private constructor. static method should be used
	private UpdateSiteDescriptorReader(InputStream updateSiteDescriptorStream, Log log) {
		this.updateSiteDescriptorStream = updateSiteDescriptorStream;
		this.log = log;
	}

	/**
	 * Reads the stream, standing for a content.xml file, and returns the
	 * repository descriptor associated with it.
	 * 
	 * @param updateSiteDescriptorStream
	 * @param log
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static RepositoryDescriptor read(InputStream updateSiteDescriptorStream, Log log) throws IOException,
			JDOMException {
		UpdateSiteDescriptorReader reader = new UpdateSiteDescriptorReader(updateSiteDescriptorStream, log);
		reader.doRead();
		return reader.repositoryDescriptor;
	}

	/**
	 * Method that does perform the reading & scanning processes.
	 * 
	 * @throws IOException
	 * @throws JDOMException
	 */
	private void doRead() throws IOException, JDOMException {
		// Opens the file.
		Document build = new SAXBuilder().build(this.updateSiteDescriptorStream);
		Element repository = build.getRootElement();
		this.repositoryDescriptor.setName(repository.getAttributeValue("name"));
		for (Object o : repository.getChild("properties").getChildren("property")) {
			Element e = (Element) o;
			if ("p2.timestamp".equals(e.getAttributeValue("name"))) {
				String attributeValue = e.getAttributeValue("value");
				repositoryDescriptor.setTimestamp(attributeValue != null && attributeValue.length() > 0 ? Long
						.parseLong(attributeValue) : 0);
			}
		}

		// Processes the units to sort them by type. Processes the ones that can
		// be processed independently.
		Set<Element> categoryUnits = new HashSet<Element>();
		Map<P2Identifier, FeatureDescriptor> featureUnits = new HashMap<P2Identifier, FeatureDescriptor>();
		Map<P2Identifier, GroupMapping> groupMappings = new HashMap<P2Identifier, GroupMapping>();

		for (Iterator<?> iterator = repository.getChild("units").getChildren("unit").iterator(); iterator.hasNext();) {
			Element unit = (Element) iterator.next();
			if (UpdateSiteDescriptorReader.isCategory(unit)) {
				String id = unit.getAttributeValue("id");
				categoryUnits.add(unit);
				this.log.info("Found Category unit with id: " + id);
			} else if (UpdateSiteDescriptorReader.isGroup(unit)) {
				String id = unit.getAttributeValue("id");
				String version = unit.getAttributeValue("version");
				groupMappings.put(new P2Identifier(id, version), toGroupMapping(unit));
				this.log.info("Found Group unit with id: " + id + " and version: " + version);
			} else if (UpdateSiteDescriptorReader.isFeature(unit)) {
				String id = unit.getAttributeValue("id");
				String version = unit.getAttributeValue("version");
				featureUnits.put(new P2Identifier(id, version), toFeatureDescriptor(unit));
				this.log.info("Found Feature unit with id: " + id + " and version: " + version);
			}
		}

		// Now process the categories and link them to the features and
		// repository
		for (Element categoryUnit : categoryUnits) {
			Element properties = categoryUnit.getChild("properties");
			String name = null;
			if (properties != null) {
				for (Iterator<?> iterator = properties.getChildren("property").iterator(); iterator.hasNext();) {
					Element property = (Element) iterator.next();
					if (name == null && "org.eclipse.equinox.p2.name".equals(property.getAttributeValue("name"))) {
						name = property.getAttributeValue("value");
					}
				}
			}

			CategoryDescriptor categoryDescriptor = new CategoryDescriptor(name);
			repositoryDescriptor.getCategoryDescriptors().add(categoryDescriptor);

			Element provides = categoryUnit.getChild("requires");
			if (provides != null) {
				for (Iterator<?> iterator = provides.getChildren("required").iterator(); iterator.hasNext();) {
					Element provided = (Element) iterator.next();
					if ("org.eclipse.equinox.p2.iu".equals(provided.getAttributeValue("namespace"))) {
						String groupName = provided.getAttributeValue("name");
						String groupRange = provided.getAttributeValue("range");
						// Coming line is a patch, that seems to come when there is aggregation...
						GroupMapping groupMapping = "1.0.0.qualifier".equals(groupRange) ? getGroupMappingByKeyOnly(
								groupMappings, groupName) : groupMappings.get(new P2Identifier(groupName,
								fromRange(groupRange)));
						if (groupMapping != null) {
							this.log.info("Found group Mapping " + groupName + " for category "
									+ categoryDescriptor.getName());
							for (P2Identifier featureId : groupMapping.getInstalledFeatureIDs()) {
								FeatureDescriptor featureDescriptor = featureUnits.get(featureId);
								if (featureDescriptor != null) {
									categoryDescriptor.getFeatureDescriptors().add(featureDescriptor);
									this.log.info("Found feature " + featureId.getName() + " for group " + groupName);
								}
							}
						}
					}
				}
			}
		}
	}

	private GroupMapping getGroupMappingByKeyOnly(Map<P2Identifier, GroupMapping> groupMappings, String groupName) {
		for (Entry<P2Identifier, GroupMapping> entry : groupMappings.entrySet()) {
			if (groupName.equals(entry.getKey().getName())) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Transforms XML Element into a group
	 * 
	 * @param unit
	 * @return
	 */
	private GroupMapping toGroupMapping(Element unit) {
		String identifier = unit.getAttributeValue("id");
		GroupMapping groupMapping = new GroupMapping(identifier);
		Element requires = unit.getChild("requires");
		if (requires != null) {
			for (Iterator<?> iterator = requires.getChildren("required").iterator(); iterator.hasNext();) {
				Element required = (Element) iterator.next();
				if ("org.eclipse.equinox.p2.iu".equals(required.getAttributeValue("namespace"))
						&& required.getChildren("filter").size() > 0) {
					String requiredName = required.getAttributeValue("name");
					String requiredValue = required.getAttributeValue("range");
					this.log.info("Added to group mapping " + identifier + " the feature with id " + requiredName);
					groupMapping.getInstalledFeatureIDs().add(new P2Identifier(requiredName, fromRange(requiredValue)));
				}
			}
		}
		return groupMapping;
	}

	/**
	 * Transforms Range value into version
	 * 
	 * @param version
	 * @return
	 */
	private String fromRange(String version) {
		if (version.startsWith("[") && version.endsWith("]") && version.indexOf(",") > 0) {
			String substring = version.substring(1, version.indexOf(","));
			return substring;
		}
		return version;
	}

	/**
	 * returns true is the unit passed as parameter is an XML element for a p2
	 * feature
	 * 
	 * @param unit
	 * @return
	 */
	private static boolean isFeature(Element unit) {
		Element provides = unit.getChild("provides");
		if (provides != null) {
			for (Iterator<?> iterator = provides.getChildren("provided").iterator(); iterator.hasNext();) {
				Element provided = (Element) iterator.next();
				if ("org.eclipse.equinox.p2.eclipse.type".equals(provided.getAttributeValue("namespace"))) {
					return "feature".equals(provided.getAttributeValue("name"));
				}
			}
		}
		return false;
	}

	/**
	 * Transforms XML Element into a feature
	 * 
	 * @param unit
	 * @return
	 */
	private FeatureDescriptor toFeatureDescriptor(Element unit) {
		String id = null;
		String name = null;
		String version = null;
		String provider = null;

		Element properties = unit.getChild("properties");
		if (properties != null) {
			for (Iterator<?> iterator = properties.getChildren("property").iterator(); iterator.hasNext();) {
				Element property = (Element) iterator.next();
				if (name == null && "org.eclipse.equinox.p2.name".equals(property.getAttributeValue("name"))) {
					name = property.getAttributeValue("value");
				}
				if (provider == null && "org.eclipse.equinox.p2.provider".equals(property.getAttributeValue("name"))) {
					provider = property.getAttributeValue("value");
				}
				if ("df_LT.featureName".equals(property.getAttributeValue("name"))) {
					name = property.getAttributeValue("value");
				}
				if ("df_LT.providerName".equals(property.getAttributeValue("name"))) {
					provider = property.getAttributeValue("value");
				}
			}
		}

		Element provides = unit.getChild("provides");
		if (provides != null) {
			for (Iterator<?> iterator = provides.getChildren("provided").iterator(); iterator.hasNext();) {
				Element provided = (Element) iterator.next();
				if ("org.eclipse.update.feature".equals(provided.getAttributeValue("namespace"))) {
					id = provided.getAttributeValue("name");
					version = provided.getAttributeValue("version");
				}
			}
		}
		return new FeatureDescriptor(id, name, version, provider);
	}

	/**
	 * returns true is the unit passed as parameter is an XML element for a p2
	 * group
	 * 
	 * @param unit
	 * @return
	 */
	private static boolean isGroup(Element unit) {
		Element properties = unit.getChild("properties");
		if (properties != null) {
			for (Iterator<?> iterator = properties.getChildren("property").iterator(); iterator.hasNext();) {
				Element property = (Element) iterator.next();
				if ("org.eclipse.equinox.p2.type.group".equals(property.getAttributeValue("name"))) {
					return "true".equals(property.getAttributeValue("value"));
				}
			}
		}
		return false;
	}

	/**
	 * returns true is the unit passed as parameter is an XML element for a p2
	 * category
	 * 
	 * @param unit
	 * @return
	 */
	private static boolean isCategory(Element unit) {
		Element properties = unit.getChild("properties");
		if (properties != null) {
			for (Iterator<?> iterator = properties.getChildren("property").iterator(); iterator.hasNext();) {
				Element property = (Element) iterator.next();
				if ("org.eclipse.equinox.p2.type.category".equals(property.getAttributeValue("name"))) {
					return "true".equals(property.getAttributeValue("value"));
				}
			}
		}
		return false;
	}

}
