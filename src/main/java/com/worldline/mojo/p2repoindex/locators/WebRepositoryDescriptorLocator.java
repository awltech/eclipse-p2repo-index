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
package com.worldline.mojo.p2repoindex.locators;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.worldline.mojo.p2repoindex.descriptors.RepositoryDescriptor;

/**
 * Implementation of the algorithm used to read repository metadata, when repository is located on the web (http access).
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public class WebRepositoryDescriptorLocator implements RepositoryDescriptorLocator {

	private final Logger logger;

	public WebRepositoryDescriptorLocator() {
		this(LoggerFactory.getLogger(WebRepositoryDescriptorLocator.class));
	}

	public WebRepositoryDescriptorLocator(Logger logger) {
		this.logger = logger;
	}

	public RepositoryDescriptor getDescriptor(String repositoryPath) {
		try {
			HttpURLConnection openedConnection = (HttpURLConnection) new URL(repositoryPath.concat("/content.xml")).openConnection();
			if (openedConnection.getResponseCode() == 200) {
				return UpdateSiteDescriptorReader.read(openedConnection.getInputStream(), this.logger);
			} else {
				HttpURLConnection openedConnection2 = (HttpURLConnection) new URL(repositoryPath.concat("/content.jar")).openConnection();
				if (openedConnection2.getResponseCode() == 200) {
					// Prepare
					InputStream is = openedConnection2.getInputStream();
					File file = new File("contents_" + System.nanoTime() + ".jar");
					file.createNewFile();
					file.deleteOnExit();
					FileOutputStream fos = new FileOutputStream(file);
					byte[] buffer = new byte[2048];
					int len = 0;
					while ((len = is.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
					ZipFile zipfile = new ZipFile(file);
					try {
						ZipEntry entry = zipfile.getEntry("content.xml");
						InputStream is2 = zipfile.getInputStream(entry);
						return UpdateSiteDescriptorReader.read(is2, this.logger);
					} finally {
						zipfile.close();
					}
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		}
		return new RepositoryDescriptor();
	}
}
