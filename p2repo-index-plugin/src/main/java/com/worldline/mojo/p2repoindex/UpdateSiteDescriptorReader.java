package com.worldline.mojo.p2repoindex;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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

	// Private constructor. static method should be used
	private UpdateSiteDescriptorReader(InputStream updateSiteDescriptorStream) {
		this.updateSiteDescriptorStream = updateSiteDescriptorStream;
	}

	/**
	 * Reads the stream, standing for a content.xml file, and returns the
	 * repository descriptor associated with it.
	 * 
	 * @param updateSiteDescriptorStream
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static RepositoryDescriptor read(InputStream updateSiteDescriptorStream) throws IOException, JDOMException {
		UpdateSiteDescriptorReader reader = new UpdateSiteDescriptorReader(updateSiteDescriptorStream);
		reader.doRead();
		return reader.repositoryDescriptor;
	}

	/**
	 * Method that does perform the reading & scanning processes.
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
				repositoryDescriptor.setTimestamp(e.getAttributeValue("value"));
			}
		}

		// Processes the units to sort them by type. Processes the ones that can be processed independently.
		Set<Element> categoryUnits = new HashSet<Element>();
		Map<P2Identifier, FeatureDescriptor> featureUnits = new HashMap<P2Identifier, FeatureDescriptor>();
		Map<P2Identifier, GroupMapping> groupMappings = new HashMap<P2Identifier, GroupMapping>();

		for (Iterator<?> iterator = repository.getChild("units").getChildren("unit").iterator(); iterator.hasNext();) {
			Element unit = (Element) iterator.next();
			if (UpdateSiteDescriptorReader.isCategory(unit)) {
				categoryUnits.add(unit);
			} else if (UpdateSiteDescriptorReader.isGroup(unit)) {
				String id = unit.getAttributeValue("id");
				String version = unit.getAttributeValue("version");
				groupMappings.put(new P2Identifier(id, version), toGroupMapping(unit));
			} else if (UpdateSiteDescriptorReader.isFeature(unit)) {
				String id = unit.getAttributeValue("id");
				String version = unit.getAttributeValue("version");
				featureUnits.put(new P2Identifier(id, version), toFeatureDescriptor(unit));
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
						GroupMapping groupMapping = groupMappings
								.get(new P2Identifier(groupName, fromRange(groupRange)));
						if (groupMapping != null) {
							for (P2Identifier featureId : groupMapping.getInstalledFeatureIDs()) {
								FeatureDescriptor featureDescriptor = featureUnits.get(featureId);
								if (featureDescriptor != null) {
									categoryDescriptor.getFeatureDescriptors().add(featureDescriptor);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Transforms XML Element into a group
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
					groupMapping.getInstalledFeatureIDs().add(new P2Identifier(requiredName, fromRange(requiredValue)));
				}
			}
		}
		return groupMapping;
	}

	/**
	 * Transforms Range value into version
	 * @param version
	 * @return
	 */
	private String fromRange(String version) {
		if (version.startsWith("[") && version.endsWith("]") && version.indexOf(",") > 0) {
			return version.substring(1, version.indexOf(","));
		}
		return version;
	}

	/**
	 * returns true is the unit passed as parameter is an XML element for a p2 feature
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
	 * returns true is the unit passed as parameter is an XML element for a p2 group
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
	 * returns true is the unit passed as parameter is an XML element for a p2 category
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
