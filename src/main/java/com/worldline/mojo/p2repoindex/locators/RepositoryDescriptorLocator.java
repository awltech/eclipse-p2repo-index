package com.worldline.mojo.p2repoindex.locators;

import com.worldline.mojo.p2repoindex.descriptors.RepositoryDescriptor;

public interface RepositoryDescriptorLocator {
	
	RepositoryDescriptor getDescriptor(String repositoryPath);
	
}
