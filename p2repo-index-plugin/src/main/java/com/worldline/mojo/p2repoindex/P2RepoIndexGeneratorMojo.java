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

@Mojo(name = "generate-index", requiresProject = false)
public class P2RepoIndexGeneratorMojo extends AbstractMojo {

	@Parameter(required = false, defaultValue = "${project}")
	private MavenProject mavenProject;

	@Parameter(required = false, property = "repositoryPath")
	private String repositoryPath;

	@Parameter(required = false, property = "documentationUrl")
	private String documentationURL;

	private SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a z");

	public void execute() throws MojoExecutionException, MojoFailureException {
		// To ensure there is no error.

		getLog().info(String.format("Input parameter [mavenProject=%s]", mavenProject));
		getLog().info(String.format("Input parameter [repositoryPath=%s]", repositoryPath));
		getLog().info(String.format("Input parameter [documentationURL=%s]", documentationURL));

		String repoPath = repositoryPath;

		if (repoPath == null && this.mavenProject != null) {
			File effectiveMavenProject = null;
			try {
				effectiveMavenProject = locateRepositoryProject(this.mavenProject.getFile());
			} catch (Exception e1) {
				e1.printStackTrace();
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

		File index = new File(repoPath.concat(File.separator).concat("index.html"));
		try {
			index.createNewFile();
		} catch (IOException e) {
			getLog().error("Could not create index file because of " + e.getMessage() + ".", e);
			return;
		}

		// Generate index.html file, to prevent from 404 error when browsing
		// repository.

		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.init();

		RepositoryDescriptor repositoryDescriptor = getRepositoryDescriptor(repoPath);
		Collections.sort(repositoryDescriptor.getFeatureDescriptors());
		try {
			VelocityContext context = new VelocityContext();
			context.put("repositoryDescriptor", repositoryDescriptor);
			context.put("projectURL", projectURL);
			context.put("dateNow", sdf.format(new Date()));
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

		try {
			File f = new File(index.getParentFile().getPath() + File.separator + "style.css");
			if (!f.exists()) {
				f.createNewFile();
				VelocityContext context = new VelocityContext();
				Template template = ve.getTemplate("style.css.vm");
				FileWriter fileWriter = new FileWriter(f);
				template.merge(context, fileWriter);
				fileWriter.close();
				getLog().info("Style file generated successfully at " + f.getPath() + ".");
			}
		} catch (IOException e) {
			getLog().error("Could not write style file because of " + e.getMessage() + ".", e);
			return;
		}

	}

	private MavenProject read(File pomFile) throws Exception {
		if (pomFile == null || !pomFile.exists()) {
			return null;
		}

		FileReader reader = new FileReader(pomFile);
		MavenProject project = new MavenProject(new MavenXpp3Reader().read(reader));
		reader.close();
		return project;
	}

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
							e.printStackTrace();
						} catch (XmlPullParserException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return null;
	}

	private RepositoryDescriptor getRepositoryDescriptor(String repoPath) {
		RepositoryDescriptor repositoryDescriptor = new RepositoryDescriptor();
		InputStream contentsFileInputStream = null;
		ZipFile zipFile = null;
		try {

			File xmlFile = new File(repoPath + "/content.xml");
			if (xmlFile != null && xmlFile.exists())
				contentsFileInputStream = new FileInputStream(xmlFile);

			File jarFile = new File(repoPath + "/content.jar");
			if (jarFile != null && jarFile.exists()) {
				zipFile = new ZipFile(jarFile);
				ZipEntry entry = zipFile.getEntry("content.xml");
				if (entry != null) {
					contentsFileInputStream = zipFile.getInputStream(entry);
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
						FeatureDescriptor featureDescriptor = new FeatureDescriptor(featureId, effectiveName,
								featureVersion, providerName);
						getLog().info("Found feature: " + featureDescriptor);
						repositoryDescriptor.getFeatureDescriptors().add(featureDescriptor);
					}
				}

				return repositoryDescriptor;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} finally {
			if (contentsFileInputStream != null) {
				try {
					contentsFileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return repositoryDescriptor;
	}

}
