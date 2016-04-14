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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.worldline.mojo.p2repoindex.P2RepoIndexGenerator;
import com.worldline.mojo.p2repoindex.locators.WebRepositoryDescriptorLocator;

/**
 * Maven Mojo that generates index.html file on Eclipse Repository, to prevent from 404 errors when trying to access site from browser.
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
@Mojo(name = "generate-index-dist", requiresProject = false)
public class DistantP2RepoIndexGeneratorMojo extends AbstractMojo {

	/**
	 * User variable, used to force repository path.
	 */
	@Parameter(required = true, property = "repositoryPath")
	private String repositoryPath;

	/**
	 * User variable, used to force project documentation URL.
	 */
	@Parameter(required = false, property = "documentationUrl")
	private String documentationURL;

	/**
	 * Path to folder where files should be generated.
	 */
	@Parameter(required = false, property = "output", defaultValue = ".")
	private String pathToOutputFolder;

	/**
	 * Build identifier
	 */
	@Deprecated
	@Parameter(required = false, property = "buildId")
	private String buildId;

	/**
	 * Build identifier
	 */
	@Parameter(required = false, property = "version")
	private String version;

	/**
	 * 
	 */
	@Parameter(required = false, property = "generateJSon", defaultValue = "false")
	private boolean generateJSon;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Locates the repository project.
		String buildId = this.version;
		if (buildId == null) {
			buildId = this.buildId;
		}
		MavenLoggerWrapper mavenLoggerWrapper = new MavenLoggerWrapper(getLog());
		P2RepoIndexGenerator p2RepoIndexGenerator = new P2RepoIndexGenerator(mavenLoggerWrapper, repositoryPath, pathToOutputFolder.concat(File.separator), documentationURL, buildId);
		p2RepoIndexGenerator.setLocator(new WebRepositoryDescriptorLocator(mavenLoggerWrapper));
		p2RepoIndexGenerator.setGenerateJSon(this.generateJSon);
		p2RepoIndexGenerator.execute();

	}
}
