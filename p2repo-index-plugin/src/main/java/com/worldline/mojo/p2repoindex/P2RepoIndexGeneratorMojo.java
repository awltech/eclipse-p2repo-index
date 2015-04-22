package com.worldline.mojo.p2repoindex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "generate-index", defaultPhase = LifecyclePhase.PACKAGE)
public class P2RepoIndexGeneratorMojo extends AbstractMojo {

	@Parameter(required = false, defaultValue = "${project}")
	private MavenProject mavenProject;

	@Parameter(required = false, property = "repositoryPath")
	private String repositoryPath;
	
	@Parameter(required = false, property = "projectName")
	private String projectName = "<PROJECT_NAME>";
	
	@Parameter(required = false, property = "documentationUrl")
	private String documentationURL = "http://www.example.org";
	

	public void execute() throws MojoExecutionException, MojoFailureException {
		// To ensure there is no error.
		
		String effectiveProjectName = projectName;
		if (effectiveProjectName == null && this.mavenProject != null) {
			effectiveProjectName = this.mavenProject.getName();
		}
		
		String repoPath = repositoryPath;
		if (repoPath == null && this.mavenProject != null) {
			String basedirPath = this.mavenProject.getBasedir().getPath();
			repoPath = basedirPath.concat(File.separator).concat("target").concat(File.separator)
					.concat("repository");
		}
		
		String projectURL = documentationURL;
		if (projectURL == null && this.mavenProject != null) {
			projectURL = this.mavenProject.getUrl();
		}
		
		if (repoPath == null) {
			getLog().error("Cannot resolve Repository Path at all. Aborts.");
			return;
		}
		
		String featuresPath = repoPath.concat(File.separator).concat("features");
		File index = new File(repoPath.concat(File.separator).concat("index.html"));
		try {
			index.createNewFile();
		} catch (IOException e) {
			getLog().error("Could not create index file because of " + e.getMessage() + ".", e);
			return;
		}

		// Generate index.html file, to prevent from 404 error when browsing
		// repository.
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(index));
			writer.write("<head></head>\n");
			writer.write("<body><div><section><h3>\n");
			writer.write("Welcome on the \"" + projectName + "\" Update site !\n");
			writer.write("</h3></section><section><p>\n");
			writer.write("<div>This page is is an Eclipse Update Site, and hence, not intended from browsing.</div>\n");
			writer.write("<div>To use it, please do into your Eclipse instance, and select the Install New Software option with this URL to access the binaries.</div>\n");
			if (projectURL != null && projectURL.length() > 0) {
				writer.write("<div>If you were looking for the documentation of this project, please click <a href=\""
						+ projectURL + "\">here</a> to be redirected..</div>\n");
			}
			writer.write("</p></section><section><p>Here are the included features:</p><p>\n");
			for (File feature : new File(featuresPath).listFiles()) {
				writer.write("<div> - " + feature.getName() + "</div>\n");
			}
			writer.write("</p></section></div></body>");
			if (getLog().isInfoEnabled())
				getLog().info("Index file generated successfully at " + index.getPath() + ".");
			writer.close();
		} catch (IOException e) {
			getLog().error("Could not write index file because of " + e.getMessage() + ".", e);
			return;
		}

	}

}
