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
	@Parameter(required = false, property = "repositoryPath", defaultValue = ".")
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
		getLog().debug(String.format("Input parameter [mavenProject=%s]", mavenProject));
		getLog().debug(String.format("Input parameter [repositoryPath=%s]", repositoryPath));
		getLog().debug(String.format("Input parameter [documentationURL=%s]", documentationURL));

		// Locates the repository project.
		String repoPath = repositoryPath;
		if (repoPath == null && this.mavenProject != null) {
			File effectiveMavenProject = null;
			try {
				effectiveMavenProject = locateRepositoryProject(this.mavenProject.getFile());
			} catch (Exception e1) {
				getLog().warn("Encountered Error while locating maven project !", e1);
			}
			if (effectiveMavenProject != null) {
				getLog().debug("Repository Path is null but not Maven Project. Will resolve Repository Path from it");
				String basedirPath = effectiveMavenProject.getPath();
				repoPath = basedirPath.concat(File.separator).concat("target").concat(File.separator)
						.concat("repository");
			}
		}
		getLog().info("Processing Eclipse repository at path: " + repoPath);

		String projectURL = documentationURL;
		if (projectURL == null && this.mavenProject != null) {
			projectURL = this.mavenProject.getUrl();
		}

		if (repoPath == null || !new File(repoPath).exists()) {
			getLog().error("Cannot resolve Repository Path. Aborts.");
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
			File index = new File(repoPath.concat(File.separator).concat("index.html"));
			index.createNewFile();
			VelocityContext context = new VelocityContext();
			context.put("repositoryDescriptor", repositoryDescriptor);
			context.put("projectURL", projectURL);
			context.put("dateNow", new Date());
			context.put("dateSite", sdf.format(new Date(Long.parseLong(repositoryDescriptor.getTimestamp()))));
			Template template = ve.getTemplate("index.html.vm");
			FileWriter fileWriter = new FileWriter(index);
			template.merge(context, fileWriter);
			fileWriter.close();
			getLog().info("Index file generated successfully at " + index.getPath() + ".");
		} catch (Exception e) {
			getLog().error("Could not write index file because of " + e.getMessage() + ".", e);
			return;
		}

		// Creates empty style.css file & generates its contents using velocity
		try {
			File f = new File(repoPath.concat(File.separator).concat("style.css"));
			f.createNewFile();
			VelocityContext context = new VelocityContext();
			context.put("dateNow", new Date());
			Template template = ve.getTemplate("style.css.vm");
			FileWriter fileWriter = new FileWriter(f);
			template.merge(context, fileWriter);
			fileWriter.close();
			getLog().info("Style file generated successfully at " + f.getPath() + ".");
		} catch (IOException e) {
			getLog().error("Could not write style file because of " + e.getMessage() + ".", e);
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
		if (mavenProject != null && "pom".equals(mavenProject.getPackaging())) {
			for (Object o : mavenProject.getModules()) {
				if (o instanceof String) {
					String moduleAsString = (String) o;
					File subPom = new File(mavenProjectFile.getParentFile().getPath() + File.separator + moduleAsString
							+ File.separator + "pom.xml");
					if (subPom.exists()) {
						try {
							FileReader fileReader = new FileReader(subPom);
							MavenProject module = new MavenProject(new MavenXpp3Reader().read(fileReader));
							if ("eclipse-repository".equals(module.getPackaging())) {
								getLog().debug("Located repository from parent: " + module.getArtifactId());
								return subPom.getParentFile();
							} else if ("pom".equals(module.getPackaging())) {
								return locateRepositoryProject(subPom);
							}
							fileReader.close();
						} catch (IOException e) {
							getLog().warn("Exception encountered while locating repository project", e);
						} catch (XmlPullParserException e) {
							getLog().warn("Exception encountered while locating repository project", e);
						}
					}
				}
			}
		}
		getLog().debug(
				"Couldn't locate any repository from parent: "
						+ (mavenProject != null ? mavenProject.getArtifactId() : "<UNDEF>"));
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
		try {
			File xmlFile = new File(repoPath + "/content.xml");
			if (xmlFile != null && xmlFile.exists()) {
				contentsFileInputStream = new FileInputStream(xmlFile);
			} else {
				File jarFile = new File(repoPath + "/content.jar");
				if (jarFile != null && jarFile.exists()) {
					zipFile = new ZipFile(jarFile);
					ZipEntry entry = zipFile.getEntry("content.xml");
					if (entry != null) {
						contentsFileInputStream = zipFile.getInputStream(entry);
					}
				}
			}
			if (contentsFileInputStream != null) {
				return UpdateSiteDescriptorReader.read(contentsFileInputStream);
			}
		} catch (IOException e) {
			getLog().warn("Encountered exception while reading repository information", e);
		} catch (JDOMException e) {
			getLog().warn("Encountered exception while reading repository information", e);
		} finally {
			if (contentsFileInputStream != null) {
				try {
					contentsFileInputStream.close();
				} catch (IOException e) {
					getLog().warn("Encountered exception while reading repository information", e);
				}
			}
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException e) {
					getLog().warn("Encountered exception while reading repository information", e);
				}
			}
		}
		return repositoryDescriptor;
	}
}
