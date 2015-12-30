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

public class P2RepoIndexGenerator {

	private final Logger logger;
	private String repositoryPath;
	private String pathToOutputFile;
	private String documentationURL;
	private RepositoryDescriptorLocator locator = new FSRepositoryDescriptorLocator();
	private String buildId;

	public P2RepoIndexGenerator(String pathToRepository, String pathToOutputFile, String documentationUrl, String buildId) {
		this(LoggerFactory.getLogger(P2RepoIndexGenerator.class), pathToRepository, pathToOutputFile, documentationUrl, buildId);
	}

	public P2RepoIndexGenerator(Logger logger, String pathToRepository, String pathToOutputFile, String documentationUrl, String buildId) {
		this.logger = logger;
		this.repositoryPath = pathToRepository;
		this.pathToOutputFile = pathToOutputFile;
		this.documentationURL = documentationUrl;
		this.buildId = buildId;
	}

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
			context.put("buildId", this.buildId);
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
