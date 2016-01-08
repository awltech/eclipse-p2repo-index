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
		RepositoryDescriptor repositoryDescriptor = new RepositoryDescriptor();
		InputStream contentsFileInputStream = null;
		ZipFile zipFile = null;
		logger.debug(Messages.LOCATING_DESCRIPTOR.value());
		try {
			File xmlFile = new File(repositoryPath + "/content.xml");
			if (xmlFile != null && xmlFile.exists()) {
				contentsFileInputStream = new FileInputStream(xmlFile);
				logger.debug(Messages.RESOLVED_DESCRIPTOR.value(xmlFile.getName()));
			} else {
				File jarFile = new File(repositoryPath + "/content.jar");
				logger.debug(Messages.RESOLVED_JAR_DESCRIPTOR.value(jarFile.getName()));
				if (jarFile != null && jarFile.exists()) {
					zipFile = new ZipFile(jarFile);
					ZipEntry entry = zipFile.getEntry("content.xml");
					if (entry != null) {
						logger.debug(Messages.RESOLVED_JAR_FILE.value(entry.getName()));
						contentsFileInputStream = zipFile.getInputStream(entry);
					} else {
						logger.warn(Messages.DESCRIPTOR_NOT_RESOLVED.value());
					}
				}
			}
			if (contentsFileInputStream != null) {
				logger.info(Messages.PROCESSING_DESCRIPTOR_CONTENTS.value());
				return UpdateSiteDescriptorReader.read(contentsFileInputStream, logger);
			}
		} catch (IOException e) {
			logger.warn(Messages.EXCEPTION_LOCATING_REPO.value(), e);
		} catch (JDOMException e) {
			logger.warn(Messages.EXCEPTION_LOCATING_REPO.value(), e);
		} finally {
			if (contentsFileInputStream != null) {
				try {
					contentsFileInputStream.close();
				} catch (IOException e) {
					logger.warn(Messages.EXCEPTION_LOCATING_REPO.value(), e);
				}
			}
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException e) {
					logger.warn(Messages.EXCEPTION_LOCATING_REPO.value(), e);
				}
			}
		}
		return repositoryDescriptor;
	}

}
