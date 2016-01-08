package com.worldline.mojo.p2repoindex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.worldline.mojo.p2repoindex.descriptors.CategoryDescriptor;
import com.worldline.mojo.p2repoindex.descriptors.RepositoryDescriptor;
import com.worldline.mojo.p2repoindex.locators.FSRepositoryDescriptorLocator;
import com.worldline.mojo.p2repoindex.locators.RepositoryDescriptorLocator;

/**
 * Implementation that generates index.html and style.css files from a P2 repository.
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public class P2RepoIndexGenerator {

	/**
	 * Logger instance
	 */
	private final Logger logger;

	/**
	 * Path to the P2 repository, it is supported to generate files in.
	 */
	private String repositoryPath;

	/**
	 * Local FS path, describing the folder where the files will be generated. Here, the value is difference than repository path, to support local
	 * generation to distant (http) repositories.
	 */
	private String pathToOutputFile;

	/**
	 * URL to the user's documentation. This is additional information that will be generated in index.html file.
	 */
	private String documentationURL;

	/**
	 * Algorithm to retrieve the P2 repository descriptors. Defaults to FileSystem repository descriptor
	 */
	private RepositoryDescriptorLocator locator = new FSRepositoryDescriptorLocator();

	/**
	 * This is additional information, used to version the generated files.
	 */
	private String version;

	/**
	 * Creates new P2 Repository Index Generator
	 * 
	 * @param pathToRepository
	 *            path to where the repository is hosted
	 * @param pathToOutputFile
	 *            path to where the generated files will be put
	 * @param documentationUrl
	 *            user's documentation URL
	 * @param version
	 *            user's document version
	 */
	public P2RepoIndexGenerator(String pathToRepository, String pathToOutputFile, String documentationUrl,
			String version) {
		this(LoggerFactory.getLogger(P2RepoIndexGenerator.class), pathToRepository, pathToOutputFile, documentationUrl,
				version);
	}

	/**
	 * Creates new P2 Repository Index Generator
	 * 
	 * @param logger
	 *            logger instance, in case we want to have special one (case with Maven plugins)
	 * @param pathToRepository
	 *            path to where the repository is hosted
	 * @param pathToOutputFile
	 *            path to where the generated files will be put
	 * @param documentationUrl
	 *            user's documentation URL
	 * @param version
	 *            user's document version
	 */
	public P2RepoIndexGenerator(Logger logger, String pathToRepository, String pathToOutputFile,
			String documentationUrl, String version) {
		this.logger = logger;
		this.repositoryPath = pathToRepository;
		this.pathToOutputFile = pathToOutputFile;
		this.documentationURL = documentationUrl;
		this.version = version;
	}

	/**
	 * Sets the algorithm to retrieve the P2 repository descriptors
	 * 
	 * @param locator
	 */
	public void setLocator(RepositoryDescriptorLocator locator) {
		this.locator = locator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() {

		SimpleDateFormat sdf = new SimpleDateFormat("EEEE d MMMM yyyy 'at' h:mm a z");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		logger.info(Messages.STARTING.value());
		// logger.info(Messages.STARTING_PARAM_PROJECT.value(this.mavenProject));
		logger.info(Messages.STARTING_PARAM_REPO.value(this.repositoryPath));
		logger.info(Messages.STARTING_PARAM_DOC.value(this.documentationURL));

		RepositoryDescriptor repositoryDescriptor = locator.getDescriptor(repositoryPath);
		Collections.sort(repositoryDescriptor.getCategoryDescriptors());
		for (CategoryDescriptor categoryDescriptor : repositoryDescriptor.getCategoryDescriptors()) {
			Collections.sort(categoryDescriptor.getFeatureDescriptors());
		}

		// Initializes Velocity
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.init();

		// Generate index.html file using velocity template
		try {
			logger.debug(Messages.START_INDEX_GEN.value());
			File index = new File(this.pathToOutputFile.concat("index.html"));
			index.createNewFile();
			VelocityContext context = new VelocityContext();
			context.put("repositoryDescriptor", repositoryDescriptor);
			context.put("projectURL", documentationURL);
			context.put("dateNow", new Date());
			context.put("dateSite", sdf.format(new Date(repositoryDescriptor.getTimestamp())));
			context.put("version", this.version);
			Template template = ve.getTemplate("index.html.vm");
			FileWriter fileWriter = new FileWriter(index);
			template.merge(context, fileWriter);
			fileWriter.close();
			logger.info(Messages.DONE_INDEX_GEN.value(index.getPath()));
		} catch (Exception e) {
			logger.error(Messages.ERROR_INDEX_GEN.value(e.getMessage()), e);
			return;
		}

		// Creates empty style.css file & generates its contents using velocity
		try {
			logger.debug(Messages.START_STYLE_GEN.value());
			File f = new File(this.pathToOutputFile.concat("style.css"));
			f.createNewFile();
			VelocityContext context = new VelocityContext();
			context.put("dateNow", new Date());
			context.put("version", this.version);
			Template template = ve.getTemplate("style.css.vm");
			FileWriter fileWriter = new FileWriter(f);
			template.merge(context, fileWriter);
			fileWriter.close();
			logger.info(Messages.DONE_STYLE_GEN.value(f.getPath()));
		} catch (IOException e) {
			logger.error(Messages.ERROR_STYLE_GEN.value(e.getMessage()), e);
			return;
		}

	}
}
