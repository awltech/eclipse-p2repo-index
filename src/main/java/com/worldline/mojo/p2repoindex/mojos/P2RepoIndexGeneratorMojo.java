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
package com.worldline.mojo.p2repoindex.mojos;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.worldline.mojo.p2repoindex.Messages;
import com.worldline.mojo.p2repoindex.P2RepoIndexGenerator;
import com.worldline.mojo.p2repoindex.locators.FSRepositoryDescriptorLocator;

/**
 * Maven Mojo that generates index.html file on Eclipse Repository, to prevent from 404 errors when trying to access site from browser.
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
@Mojo(name = "generate-index", requiresProject = false)
public class P2RepoIndexGeneratorMojo extends AbstractMojo {

	/**
	 * Maven project gathered from Mojo execution. Used to locate repository when URL is not explicitly specified.
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

	/**
	 * Build identifier
	 */
	@Parameter(required = false, property = "buildId")
	private String buildId;

	/**
	 * Build identifier
	 */
	@Parameter(required = false, property = "version")
	private String version;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

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

		String buildId = version;
		if (buildId == null) {
			buildId = this.buildId != null && this.buildId.length() > 0 ? this.buildId : new SimpleDateFormat(
					"yyyyMMddHHmmssSSS").format(new Date());
		}
		MavenLoggerWrapper mavenLoggerWrapper = new MavenLoggerWrapper(getLog());
		P2RepoIndexGenerator p2RepoIndexGenerator = new P2RepoIndexGenerator(mavenLoggerWrapper, repoPath,
				repoPath.concat(File.separator), projectURL, buildId);
		p2RepoIndexGenerator.setLocator(new FSRepositoryDescriptorLocator(mavenLoggerWrapper));
		p2RepoIndexGenerator.execute();

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
	 * Locates the repository project from its parent, identified by parent's pom file passed as parameter
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
}
