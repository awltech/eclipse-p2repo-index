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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.worldline.mojo.p2repoindex.Messages;
import com.worldline.mojo.p2repoindex.descriptors.RepositoryDescriptor;

/**
 * Implementation of the algorithm used to read repository metadata, when repository is located on FileSystem.
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public class FSRepositoryDescriptorLocator implements RepositoryDescriptorLocator {

	private final Logger logger;

	public FSRepositoryDescriptorLocator() {
		this(LoggerFactory.getLogger(FSRepositoryDescriptorLocator.class));
	}

	public FSRepositoryDescriptorLocator(Logger logger) {
		this.logger = logger;
	}

	public RepositoryDescriptor getDescriptor(String repositoryPath) {
		File xmlFile = new File(repositoryPath + "/content.xml");
		if (xmlFile.exists()) {
			logger.info("Found content.xml at specified path. Will use it as site descriptor");
			return getContentXmlDescriptor(xmlFile);
		}
		File jarFile = new File(repositoryPath + "/content.jar");
		if (jarFile.exists()) {
			logger.info("Found content.jar at specified path. Will use it as site descriptor");
			return getContentJarDescriptor(jarFile);
		}
		File aggrXmlFile = new File(repositoryPath + "/compositeContent.xml");
		if (aggrXmlFile.exists()) {
			logger.info("Found compositeContent.xml at specified path. Will use it as site descriptor");
			return getCompositeContentXmlDescriptor(aggrXmlFile);
		}
		File aggrJarFile = new File(repositoryPath + "/compositeContent.jar");
		if (aggrJarFile.exists()) {
			logger.info("Found compositeContent.jar at specified path. Will use it as site descriptor");
			return getCompositeContentJarDescriptor(aggrJarFile);
		}
		
		return null;
	}

	private RepositoryDescriptor getContentXmlDescriptor(File xmlFile) {
		FileInputStream xmlFileInputStream = null;
		try {
			xmlFileInputStream = new FileInputStream(xmlFile);
			return UpdateSiteDescriptorReader.read(xmlFileInputStream, logger);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		} catch (JDOMException e) {
			logger.warn(e.getMessage(), e);
		} finally {
			if (xmlFileInputStream != null) {
				try {
					xmlFileInputStream.close();
				} catch (IOException e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
		return null;
	}

	private RepositoryDescriptor getContentJarDescriptor(File jarFile) {
		InputStream contentsFileInputStream = null;
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(jarFile);
			ZipEntry entry = zipFile.getEntry("content.xml");
			if (entry != null) {
				logger.debug(Messages.RESOLVED_JAR_FILE.value(entry.getName()));
				contentsFileInputStream = zipFile.getInputStream(entry);
				return UpdateSiteDescriptorReader.read(contentsFileInputStream, logger);
			} else {
				logger.warn(Messages.DESCRIPTOR_NOT_RESOLVED.value());
			}
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		} catch (JDOMException e) {
			logger.warn(e.getMessage(), e);
		} finally {
			if (contentsFileInputStream != null) {
				try {
					contentsFileInputStream.close();
				} catch (IOException e) {
					logger.warn(e.getMessage(), e);
				}
			}
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
		return null;
	}

	private RepositoryDescriptor getCompositeContentXmlDescriptor(File aggrXmlFile) {
		// TODO Auto-generated method stub
		return null;
	}

	private RepositoryDescriptor getCompositeContentJarDescriptor(File aggrJarFile) {
		// TODO Auto-generated method stub
		return null;
	}

}
