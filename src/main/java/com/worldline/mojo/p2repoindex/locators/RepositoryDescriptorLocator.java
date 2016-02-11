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

import com.worldline.mojo.p2repoindex.descriptors.RepositoryDescriptor;

/**
 * Interface for Repository Descriptor Locator implementation.
 * 
 * the various implementation will tell how to retrieve the repository
 * descriptors (content.xml, content.jar etc...) from the repository path,
 * retrieved from parent pom or set by the implementation invoker
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public interface RepositoryDescriptorLocator {

	/**
	 * Retrieves the repository descriptor (content.xml, content.jar,...) for
	 * the repository at provided path
	 * 
	 * @param repositoryPath
	 * @return
	 */
	RepositoryDescriptor getDescriptor(String repositoryPath);

}
