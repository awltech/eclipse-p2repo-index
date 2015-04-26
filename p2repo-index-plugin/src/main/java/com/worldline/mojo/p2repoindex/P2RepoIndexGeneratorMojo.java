package com.worldline.mojo.p2repoindex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
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

	public void execute() throws MojoExecutionException, MojoFailureException {
		// To ensure there is no error.

		String repoPath = repositoryPath;
		if (repoPath == null && this.mavenProject != null) {
			String basedirPath = this.mavenProject.getBasedir().getPath();
			repoPath = basedirPath.concat(File.separator).concat("target").concat(File.separator).concat("repository");
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

		RepositoryDescriptor repositoryDescriptor = getRepositoryDeescriptor(repoPath);
		Collections.sort(repositoryDescriptor.getFeatureDescriptors());

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(index));
			writer.write("<head><title>" + repositoryDescriptor.getName() + " Eclipse Update Site</title></head>\n");
			writer.write("<body><div><section><h3>\n");
			writer.write("Welcome on the \"" + repositoryDescriptor.getName() + "\" Eclipse Update Site !\n");
			writer.write("</h3></section><section><p>\n");
			writer.write("<div>As this page is an Eclipse Update Site, it is not intended from browsing.</div>\n");
			writer.write("<div>To use it, please go into your Eclipse instance, and select the <b>Install New Software</b> option with this URL to access the binaries.</div>\n");
			if (projectURL != null && projectURL.length() > 0) {
				writer.write("<div>If you were looking for the documentation of this project, please click <a href=\""
						+ projectURL + "\">here</a> to be redirected.</div>\n");
			}
			writer.write("</p></section><section><p><b>Available features within this update site:</b></p><p>\n");

			writer.write("<table border=\"1\">\n");
			writer.write("<tr><th>Name</th><th>Identifier</th><th>Version</th></tr>\n");
			for (FeatureDescriptor feature : repositoryDescriptor.getFeatureDescriptors()) {
				writer.write(String.format("<tr><td><b>%s</b></td><td>%s</td><td>%s</td></tr>\n", feature.getName(),
						feature.getId(), feature.getVersion()));
			}
			writer.write("</table>\n");

			writer.write("</p><p><div><i>(Generated by <a href= \"https://github.com/awltech/eclipse-p2repo-index\">AWLTech p2repo-index-plugin</a> on "
					+ /* new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format( */new Date()/* ) */+ ".)</i></div>");
			writer.write("</p></section></div></body>");
			if (getLog().isInfoEnabled())
				getLog().info("Index file generated successfully at " + index.getPath() + ".");
			writer.close();
		} catch (IOException e) {
			getLog().error("Could not write index file because of " + e.getMessage() + ".", e);
			return;
		}

	}

	private RepositoryDescriptor getRepositoryDeescriptor(String repoPath) {
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

				Element units = repository.getChild("units");
				for (Object o : units.getChildren("unit")) {
					Element unit = (Element) o;
					Element unitProperties = unit.getChild("properties");
					Element unitProvides = unit.getChild("provides");

					String featureName = "<UNDEF>";
					String featureVersion = "<UNDEF>";
					String featureId = "<UNDEF>";
					boolean isFeature = false;

					for (Object o1 : unitProperties.getChildren("property")) {
						Element property = (Element) o1;
						if ("org.eclipse.equinox.p2.name".equals(property.getAttributeValue("name"))) {
							featureName = property.getAttributeValue("value");
						}
					}

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

					if (isFeature) {
						System.out.println("Found feature: " + featureId + ", " + featureName + ", " + featureVersion);
						repositoryDescriptor.getFeatureDescriptors().add(
								new FeatureDescriptor(featureId, featureName, featureVersion));
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
