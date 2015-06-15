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
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

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
	 * when URL is not explicitely specified.
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

	private static final String VERSION = "0.1.3-SNAPSHOT";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a z");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		getLog().info(String.format("Input parameter [mavenProject=%s]", mavenProject));
		getLog().info(String.format("Input parameter [repositoryPath=%s]", repositoryPath));
		getLog().info(String.format("Input parameter [documentationURL=%s]", documentationURL));

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
				getLog().info("Repository Path URL is now " + repoPath);
			}
		}

		String projectURL = documentationURL;
		if (projectURL == null && this.mavenProject != null) {
			projectURL = this.mavenProject.getUrl();
		}

		if (repoPath == null || !new File(repoPath).exists()) {
			getLog().error("Cannot resolve Repository Path at all. Aborts.");
			return;
		}

		RepositoryDescriptor repositoryDescriptor = getRepositoryDescriptor(repoPath);
		Collections.sort(repositoryDescriptor.getFeatureDescriptors());

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
			context.put("version", VERSION);
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
			context.put("version", VERSION);
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
								getLog().info("Located repository from parent: " + module.getArtifactId());
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
				Document build = new SAXBuilder().build(contentsFileInputStream);
				Element repository = build.getRootElement();

				repositoryDescriptor.setName(repository.getAttributeValue("name"));

				for (Object o : repository.getChild("properties").getChildren("property")) {
					Element e = (Element) o;
					if ("p2.timestamp".equals(e.getAttributeValue("name"))) {
						repositoryDescriptor.setTimestamp(e.getAttributeValue("value"));
					}
				}

				Element units = repository.getChild("units");
				for (Object o : units.getChildren("unit")) {
					Element unit = (Element) o;
					Element unitProperties = unit.getChild("properties");
					Element unitProvides = unit.getChild("provides");

					String featureName = null;
					String featureVersion = null;
					String featureId = null;
					String featureI18nId = null;
					String providerName = null;
					String providerI18NName = null;
					boolean isFeature = false;
					if (unitProperties != null) {
						for (Object o1 : unitProperties.getChildren("property")) {
							Element property = (Element) o1;
							if ("org.eclipse.equinox.p2.name".equals(property.getAttributeValue("name"))) {
								featureName = property.getAttributeValue("value");
							}
							if ("org.eclipse.equinox.p2.provider".equals(property.getAttributeValue("name"))) {
								providerName = property.getAttributeValue("value");
							}
							if ("df_LT.featureName".equals(property.getAttributeValue("name"))) {
								featureI18nId = property.getAttributeValue("value");
							}
							if ("df_LT.providerName".equals(property.getAttributeValue("name"))) {
								providerI18NName = property.getAttributeValue("value");
							}
						}
					}
					if (unitProvides != null) {
						for (Object o2 : unitProvides.getChildren("provided")) {
							Element provided = (Element) o2;
							if ("org.eclipse.equinox.p2.eclipse.type".equals(provided.getAttributeValue("namespace"))
									&& "feature".equals(provided.getAttributeValue("name"))) {
								isFeature = true;
							}
							if ("org.eclipse.update.feature".equals(provided.getAttributeValue("namespace"))) {
								featureId = provided.getAttributeValue("name");
								featureVersion = provided.getAttributeValue("version");
							}
						}
					}
					if (isFeature) {
						String effectiveName = featureI18nId != null ? featureI18nId : featureName;
						String effectiveProvider = providerI18NName != null ? providerI18NName : providerName;
						FeatureDescriptor featureDescriptor = new FeatureDescriptor(featureId, effectiveName,
								featureVersion, effectiveProvider);
						getLog().info("Found feature: " + featureDescriptor);
						repositoryDescriptor.getFeatureDescriptors().add(featureDescriptor);
					}
				}

				return repositoryDescriptor;
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
