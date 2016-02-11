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
package com.worldline.mojo.p2repoindex.locators;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;

import com.worldline.mojo.p2repoindex.Messages;
import com.worldline.mojo.p2repoindex.descriptors.CategoryDescriptor;
import com.worldline.mojo.p2repoindex.descriptors.FeatureDescriptor;
import com.worldline.mojo.p2repoindex.descriptors.GroupMapping;
import com.worldline.mojo.p2repoindex.descriptors.P2Identifier;
import com.worldline.mojo.p2repoindex.descriptors.RepositoryDescriptor;

/**
 * Utility class that takes a content.xml stream as input, and returns a descriptors object graph, for this repository.
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public class UpdateSiteDescriptorReader {

	private static final String COMMA = ",";

	private static final String CLOSING_BRACKET = "]";

	private static final String OPENING_BRACKET = "[";

	private static final String CATEGORY_TYPE = "org.eclipse.equinox.p2.type.category";

	private static final String TRUE = "true";

	private static final String GROUP_TYPE = "org.eclipse.equinox.p2.type.group";

	private static final String FEATURE_TYPE = "org.eclipse.update.feature";

	private static final String I18N_PROVIDER_NAME_VARIABLE = "df_LT.providerName";

	private static final String I18N_FEATURE_NAME_VARIABLE = "df_LT.featureName";

	private static final String P2_PROVIDER_VARIABLE = "org.eclipse.equinox.p2.provider";

	private static final String FEATURE_KEYWORD = "feature";

	private static final String P2_ECLIPSE_TYPE_VARIABLE = "org.eclipse.equinox.p2.eclipse.type";

	private static final String PROVIDED_VARIABLE = "provided";

	private static final String PROVIDES_VARIABLE = "provides";

	private static final String FILTER_VARIABLE = "filter";

	private static final String RANGE_VARIABLE = "range";

	private static final String P2_IU_VARIABLE = "org.eclipse.equinox.p2.iu";

	private static final String NAMESPACE_VARIABLE = "namespace";

	private static final String REQUIRED_VARIABLE = "required";

	private static final String REQUIRES_VARIABLE = "requires";

	private static final String P2_NAME_VARIABLE = "org.eclipse.equinox.p2.name";

	private static final String VERSION_VARIABLE = "version";

	private static final String ID_VARIABLE = "id";

	private static final String UNIT_VARIABLE = "unit";

	private static final String UNITS_VARIABLE = "units";

	private static final String VALUE_VARIABLE = "value";

	private static final String P2_TIMESTAMP = "p2.timestamp";

	private static final String PROPERTY_VARIABLE = "property";

	private static final String PROPERTIES_VARIABLE = "properties";

	private static final String NAME_VARIABLE = "name";

	/**
	 * Input of the processus
	 */
	private final InputStream[] updateSiteDescriptorStreams;

	/**
	 * Output of the processus.
	 */
	private final RepositoryDescriptor repositoryDescriptor = new RepositoryDescriptor();

	/**
	 * Logger instance
	 */
	private Logger log;

	// Private constructor. static method should be used
	private UpdateSiteDescriptorReader(InputStream[] updateSiteDescriptorStreams, Logger log) {
		this.updateSiteDescriptorStreams = updateSiteDescriptorStreams;
		this.log = log;
	}

	/**
	 * Reads the stream, standing for a content.xml file, and returns the repository descriptor associated with it.
	 * 
	 * @param updateSiteDescriptorStream
	 * @param log
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static RepositoryDescriptor read(InputStream updateSiteDescriptorStream, Logger log) throws IOException, JDOMException {
		return read(new InputStream[] { updateSiteDescriptorStream }, log);
	}

	/**
	 * Reads the stream, standing for a content.xml file, and returns the repository descriptor associated with it.
	 * 
	 * @param updateSiteDescriptorStream
	 * @param log
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static RepositoryDescriptor read(InputStream[] updateSiteDescriptorStreams, Logger log) throws IOException, JDOMException {
		UpdateSiteDescriptorReader reader = new UpdateSiteDescriptorReader(updateSiteDescriptorStreams, log);
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

		Set<Element> categoryUnits = new HashSet<Element>();
		Map<P2Identifier, FeatureDescriptor> featureUnits = new HashMap<P2Identifier, FeatureDescriptor>();
		Map<P2Identifier, GroupMapping> groupMappings = new HashMap<P2Identifier, GroupMapping>();

		for (InputStream updateSiteDescriptorStream : this.updateSiteDescriptorStreams) {
			Document build = new SAXBuilder().build(updateSiteDescriptorStream);
			Element repository = build.getRootElement();
			if (this.repositoryDescriptor.getName() == null) {
				this.repositoryDescriptor.setName(repository.getAttributeValue(NAME_VARIABLE));
			}

			if (this.repositoryDescriptor.getTimestamp() == 0) {
				for (Object o : repository.getChild(PROPERTIES_VARIABLE).getChildren(PROPERTY_VARIABLE)) {
					Element e = (Element) o;
					if (P2_TIMESTAMP.equals(e.getAttributeValue(NAME_VARIABLE))) {
						String attributeValue = e.getAttributeValue(VALUE_VARIABLE);
						repositoryDescriptor.setTimestamp(attributeValue != null && attributeValue.length() > 0 ? Long.parseLong(attributeValue) : 0);
					}
				}
			}

			// Processes the units to sort them by type. Processes the ones that can
			// be processed independently.

			for (Iterator<?> iterator = repository.getChild(UNITS_VARIABLE).getChildren(UNIT_VARIABLE).iterator(); iterator.hasNext();) {
				Element unit = (Element) iterator.next();
				if (UpdateSiteDescriptorReader.isCategory(unit)) {
					String id = unit.getAttributeValue(ID_VARIABLE);
					categoryUnits.add(unit);
					this.log.debug(Messages.CREATING_UNIT_CATEGORY.value(id));
				} else if (UpdateSiteDescriptorReader.isGroup(unit)) {
					String id = unit.getAttributeValue(ID_VARIABLE);
					String version = unit.getAttributeValue(VERSION_VARIABLE);
					groupMappings.put(new P2Identifier(id, version), toGroupMapping(unit));
					this.log.debug(Messages.CREATING_UNIT_GROUP.value(id, version));
				} else if (UpdateSiteDescriptorReader.isFeature(unit)) {
					String id = unit.getAttributeValue(ID_VARIABLE);
					String version = unit.getAttributeValue(VERSION_VARIABLE);
					featureUnits.put(new P2Identifier(id, version), toFeatureDescriptor(unit));
					this.log.debug(Messages.CREATING_UNIT_FEATURE.value(id, version));
				}
			}
		}
		// Now process the categories and link them to the features and
		// repository
		for (Element categoryUnit : categoryUnits) {
			Element properties = categoryUnit.getChild(PROPERTIES_VARIABLE);
			String name = null;
			if (properties != null) {
				for (Iterator<?> iterator = properties.getChildren(PROPERTY_VARIABLE).iterator(); iterator.hasNext();) {
					Element property = (Element) iterator.next();
					if (name == null && P2_NAME_VARIABLE.equals(property.getAttributeValue(NAME_VARIABLE))) {
						name = property.getAttributeValue(VALUE_VARIABLE);
					}
				}
			}

			CategoryDescriptor categoryDescriptor = new CategoryDescriptor(name);
			repositoryDescriptor.getCategoryDescriptors().add(categoryDescriptor);
			this.log.debug(Messages.CREATED_CATEGORY.value(name));

			Element provides = categoryUnit.getChild(REQUIRES_VARIABLE);
			if (provides != null) {
				for (Iterator<?> iterator = provides.getChildren(REQUIRED_VARIABLE).iterator(); iterator.hasNext();) {
					Element provided = (Element) iterator.next();
					if (P2_IU_VARIABLE.equals(provided.getAttributeValue(NAMESPACE_VARIABLE))) {
						String groupName = provided.getAttributeValue(NAME_VARIABLE);
						String groupRange = provided.getAttributeValue(RANGE_VARIABLE);
						// Coming line is a patch, that seems to come when there
						// is aggregation...
						GroupMapping groupMapping = groupMappings.get(new P2Identifier(groupName, fromRange(groupRange)));
						if (groupMapping == null) {
							groupMapping = getGroupMappingByKeyOnly(groupMappings, groupName);
						}
						if (groupMapping != null) {
							this.log.debug(Messages.FOUND_GROUP_MAPPING.value(groupName, categoryDescriptor.getName()));
							for (P2Identifier featureId : groupMapping.getInstalledFeatureIDs()) {
								FeatureDescriptor featureDescriptor = featureUnits.get(featureId);
								if (featureDescriptor != null) {
									categoryDescriptor.getFeatureDescriptors().add(featureDescriptor);
									this.log.info(Messages.ADDED_FEATURE.value(featureDescriptor.getName(), categoryDescriptor.getName()));
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
		String identifier = unit.getAttributeValue(ID_VARIABLE);
		GroupMapping groupMapping = new GroupMapping(identifier);
		Element requires = unit.getChild(REQUIRES_VARIABLE);
		if (requires != null) {
			for (Iterator<?> iterator = requires.getChildren(REQUIRED_VARIABLE).iterator(); iterator.hasNext();) {
				Element required = (Element) iterator.next();
				if (P2_IU_VARIABLE.equals(required.getAttributeValue(NAMESPACE_VARIABLE)) && required.getChildren(FILTER_VARIABLE).size() > 0) {
					String requiredName = required.getAttributeValue(NAME_VARIABLE);
					String requiredValue = required.getAttributeValue(RANGE_VARIABLE);
					this.log.debug(Messages.FEATURE_ADDED_TO_GROUP.value(identifier, requiredName));
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
		if (version.startsWith(OPENING_BRACKET) && version.endsWith(CLOSING_BRACKET) && version.indexOf(COMMA) > 0) {
			String substring = version.substring(1, version.indexOf(COMMA));
			return substring;
		}
		return version;
	}

	/**
	 * returns true is the unit passed as parameter is an XML element for a p2 feature
	 * 
	 * @param unit
	 * @return
	 */
	private static boolean isFeature(Element unit) {
		Element provides = unit.getChild(PROVIDES_VARIABLE);
		if (provides != null) {
			for (Iterator<?> iterator = provides.getChildren(PROVIDED_VARIABLE).iterator(); iterator.hasNext();) {
				Element provided = (Element) iterator.next();
				if (P2_ECLIPSE_TYPE_VARIABLE.equals(provided.getAttributeValue(NAMESPACE_VARIABLE))) {
					return FEATURE_KEYWORD.equals(provided.getAttributeValue(NAME_VARIABLE));
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

		Element properties = unit.getChild(PROPERTIES_VARIABLE);
		if (properties != null) {
			for (Iterator<?> iterator = properties.getChildren(PROPERTY_VARIABLE).iterator(); iterator.hasNext();) {
				Element property = (Element) iterator.next();
				if (name == null && P2_NAME_VARIABLE.equals(property.getAttributeValue(NAME_VARIABLE))) {
					name = property.getAttributeValue(VALUE_VARIABLE);
				}
				if (provider == null && P2_PROVIDER_VARIABLE.equals(property.getAttributeValue(NAME_VARIABLE))) {
					provider = property.getAttributeValue(VALUE_VARIABLE);
				}
				if (I18N_FEATURE_NAME_VARIABLE.equals(property.getAttributeValue(NAME_VARIABLE))) {
					name = property.getAttributeValue(VALUE_VARIABLE);
				}
				if (I18N_PROVIDER_NAME_VARIABLE.equals(property.getAttributeValue(NAME_VARIABLE))) {
					provider = property.getAttributeValue(VALUE_VARIABLE);
				}
			}
		}

		Element provides = unit.getChild(PROVIDES_VARIABLE);
		if (provides != null) {
			for (Iterator<?> iterator = provides.getChildren(PROVIDED_VARIABLE).iterator(); iterator.hasNext();) {
				Element provided = (Element) iterator.next();
				if (FEATURE_TYPE.equals(provided.getAttributeValue(NAMESPACE_VARIABLE))) {
					id = provided.getAttributeValue(NAME_VARIABLE);
					version = provided.getAttributeValue(VERSION_VARIABLE);
				}
			}
		}
		return new FeatureDescriptor(id, name, version, provider);
	}

	/**
	 * returns true is the unit passed as parameter is an XML element for a p2 group
	 * 
	 * @param unit
	 * @return
	 */
	private static boolean isGroup(Element unit) {
		Element properties = unit.getChild(PROPERTIES_VARIABLE);
		if (properties != null) {
			for (Iterator<?> iterator = properties.getChildren(PROPERTY_VARIABLE).iterator(); iterator.hasNext();) {
				Element property = (Element) iterator.next();
				if (GROUP_TYPE.equals(property.getAttributeValue(NAME_VARIABLE))) {
					return TRUE.equals(property.getAttributeValue(VALUE_VARIABLE));
				}
			}
		}
		return false;
	}

	/**
	 * returns true is the unit passed as parameter is an XML element for a p2 category
	 * 
	 * @param unit
	 * @return
	 */
	private static boolean isCategory(Element unit) {
		Element properties = unit.getChild(PROPERTIES_VARIABLE);
		if (properties != null) {
			for (Iterator<?> iterator = properties.getChildren(PROPERTY_VARIABLE).iterator(); iterator.hasNext();) {
				Element property = (Element) iterator.next();
				if (CATEGORY_TYPE.equals(property.getAttributeValue(NAME_VARIABLE))) {
					return TRUE.equals(property.getAttributeValue(VALUE_VARIABLE));
				}
			}
		}
		return false;
	}

}
