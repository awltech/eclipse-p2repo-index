package com.worldline.mojo.p2repoindex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jdom.JDOMException;
import com.worldline.mojo.p2repoindex.Messages;

/**
 * Maven Mojo that generates index.html file on Eclipse Repository, to prevent
 * from 404 errors when trying to access site from browser.
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
@Mojo(name = "generate-index", requiresProject = false)
public class P2RepoIndexGeneratorMojo extends AbstractMojo {

	/**
	 * Maven project gathered from Mojo execution. Used to locate repository
	 * when URL is not explicitly specified.
	 */
	@Parameter(required = false, defaultValue = "${project}")
	private MavenProject mavenProject;

	/**
	 * User variable, used to force repository path
	 */
	@Parameter(required = false, property = "repositoryPath")
	private String repositoryPath;

	/**
	 * User variable, used to force project documentation URL
	 */
	@Parameter(required = false, property = "documentationUrl")
	private String documentationURL;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		SimpleDateFormat sdf = new SimpleDateFormat("EEEE d MMMM yyyy 'at' h:mm a z");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		getLog().info(Messages.STARTING.value());
		getLog().info(Messages.STARTING_PARAM_PROJECT.value(this.mavenProject));
		getLog().info(Messages.STARTING_PARAM_REPO.value(this.repositoryPath));
		getLog().info(Messages.STARTING_PARAM_DOC.value(this.documentationURL));

		// Locates the repository project.
		String repoPath = repositoryPath;
		if (repoPath == null && this.mavenProject != null) {
			getLog().debug(Messages.REPO_PATH_NOT_SPECIFIED.value());
			File effectiveMavenProject = null;
			try {
				effectiveMavenProject = locateRepositoryProject(this.mavenProject.getFile());
				getLog().debug(Messages.REPO_PROJECT_FOUND.value(effectiveMavenProject));
			} catch (Exception e1) {
				getLog().warn(Messages.ERROR_ENCOUNTERED.value(), e1);
			}
			if (effectiveMavenProject != null) {
				String basedirPath = effectiveMavenProject.getPath();
				repoPath = basedirPath.concat(File.separator).concat("target").concat(File.separator)
						.concat("repository");
				getLog().debug(Messages.REPO_FOLDER_FOUND.value(repoPath));
			}
		}
		getLog().info(Messages.PROCESSING_REPOSITORY.value(repoPath));

		String projectURL = documentationURL;
		if (projectURL == null && this.mavenProject != null) {
			projectURL = this.mavenProject.getUrl();
		}

		if (repoPath == null || !new File(repoPath).exists() || !new File(repoPath).isDirectory()) {
			getLog().error(Messages.ABORT_PATH_NULL.value());
			return;
		}

		RepositoryDescriptor repositoryDescriptor = getRepositoryDescriptor(repoPath);
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
			getLog().debug(Messages.START_INDEX_GEN.value());
			File index = new File(repoPath.concat(File.separator).concat("index.html"));
			index.createNewFile();
			VelocityContext context = new VelocityContext();
			context.put("repositoryDescriptor", repositoryDescriptor);
			context.put("projectURL", projectURL);
			context.put("dateNow", new Date());
			context.put("dateSite", sdf.format(new Date(repositoryDescriptor.getTimestamp())));
			Template template = ve.getTemplate("index.html.vm");
			FileWriter fileWriter = new FileWriter(index);
			template.merge(context, fileWriter);
			fileWriter.close();
			getLog().info(Messages.DONE_INDEX_GEN.value(index.getPath()));
		} catch (Exception e) {
			getLog().error(Messages.ERROR_INDEX_GEN.value(e.getMessage()), e);
			return;
		}

		// Creates empty style.css file & generates its contents using velocity
		try {
			getLog().debug(Messages.START_STYLE_GEN.value());
			File f = new File(repoPath.concat(File.separator).concat("style.css"));
			f.createNewFile();
			VelocityContext context = new VelocityContext();
			context.put("dateNow", new Date());
			Template template = ve.getTemplate("style.css.vm");
			FileWriter fileWriter = new FileWriter(f);
			template.merge(context, fileWriter);
			fileWriter.close();
			getLog().info(Messages.DONE_STYLE_GEN.value(f.getPath()));
		} catch (IOException e) {
			getLog().error(Messages.ERROR_STYLE_GEN.value(e.getMessage()), e);
			return;
		}

	}

	/**
	 * Takes files and loads it into Maven project.
	 * 
	 * @param pomFile
	 * @return
	 * @throws Exception
	 */
	private MavenProject read(File pomFile) throws Exception {
		if (pomFile == null || !pomFile.exists()) {
			return null;
		}

		FileReader reader = new FileReader(pomFile);
		MavenProject project = new MavenProject(new MavenXpp3Reader().read(reader));
		reader.close();
		return project;
	}

	/**
	 * Locates the repository project from its parent, identified by parent's
	 * pom file passed as parameter
	 * 
	 * @param mavenProjectFile
	 * @return
	 * @throws Exception
	 */
	private File locateRepositoryProject(File mavenProjectFile) throws Exception {
		if (mavenProject == null) {
			return null;
		}
		MavenProject mavenProject = read(mavenProjectFile);
		if (mavenProject != null) {
			if ("pom".equals(mavenProject.getPackaging())) {
				for (Object o : mavenProject.getModules()) {
					if (o instanceof String) {
						String moduleAsString = (String) o;
						File subPom = new File(mavenProjectFile.getParentFile().getPath() + File.separator
								+ moduleAsString + File.separator + "pom.xml");
						if (subPom.exists()) {
							try {
								FileReader fileReader = new FileReader(subPom);
								MavenProject module = new MavenProject(new MavenXpp3Reader().read(fileReader));
								if ("eclipse-repository".equals(module.getPackaging())) {
									getLog().debug("Located repository in Module: " + module.getArtifactId());
									return subPom.getParentFile();
								} else if ("pom".equals(module.getPackaging())) {
									return locateRepositoryProject(subPom);
								}
								fileReader.close();
							} catch (IOException e) {
								getLog().warn(Messages.EXCEPTION_LOCATING_REPO.value(), e);
							} catch (XmlPullParserException e) {
								getLog().warn(Messages.EXCEPTION_LOCATING_REPO.value(), e);
							}
						}
					}
				}
			} else if ("eclipse-repository".equals(mavenProject.getPackaging())) {
				getLog().debug(Messages.CURRENT_PROJ_IS_REPO.value());
				return mavenProjectFile.getParentFile();
			}
		}
		getLog().warn(
				Messages.WARN_REPO_NOT_FOUND.value(mavenProject != null ? mavenProject.getArtifactId() : "<UNDEF>"));
		return null;
	}

	/**
	 * Loads data model, describing the repository, from the content.xml
	 * contents
	 * 
	 * @param repoPath
	 * @return
	 */
	private RepositoryDescriptor getRepositoryDescriptor(String repoPath) {
		RepositoryDescriptor repositoryDescriptor = new RepositoryDescriptor();
		InputStream contentsFileInputStream = null;
		ZipFile zipFile = null;
		getLog().debug(Messages.LOCATING_DESCRIPTOR.value());
		try {
			File xmlFile = new File(repoPath + "/content.xml");
			if (xmlFile != null && xmlFile.exists()) {
				contentsFileInputStream = new FileInputStream(xmlFile);
				getLog().debug(Messages.RESOLVED_DESCRIPTOR.value(xmlFile.getName()));
			} else {
				File jarFile = new File(repoPath + "/content.jar");
				getLog().debug(
						Messages.RESOLVED_JAR_DESCRIPTOR.value(jarFile.getName()));
				if (jarFile != null && jarFile.exists()) {
					zipFile = new ZipFile(jarFile);
					ZipEntry entry = zipFile.getEntry("content.xml");
					if (entry != null) {
						getLog().debug(Messages.RESOLVED_JAR_FILE.value(entry.getName()));
						contentsFileInputStream = zipFile.getInputStream(entry);
					} else {
						getLog().warn(Messages.DESCRIPTOR_NOT_RESOLVED.value());
					}
				}
			}
			if (contentsFileInputStream != null) {
				getLog().info(Messages.PROCESSING_DESCRIPTOR_CONTENTS.value());
				return UpdateSiteDescriptorReader.read(contentsFileInputStream, getLog());
			}
		} catch (IOException e) {
			getLog().warn(Messages.EXCEPTION_LOCATING_REPO.value(), e);
		} catch (JDOMException e) {
			getLog().warn(Messages.EXCEPTION_LOCATING_REPO.value(), e);
		} finally {
			if (contentsFileInputStream != null) {
				try {
					contentsFileInputStream.close();
				} catch (IOException e) {
					getLog().warn(Messages.EXCEPTION_LOCATING_REPO.value(), e);
				}
			}
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException e) {
					getLog().warn(Messages.EXCEPTION_LOCATING_REPO.value(), e);
				}
			}
		}
		return repositoryDescriptor;
	}
}
